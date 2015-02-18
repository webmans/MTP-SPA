$(document).ready(function() {
	refreshCam = function() {
		id = new Date().getTime();
		
		$('.cam').addClass('old-' + id);
		
		$('.camera_picture .box').append('<img id="cam_' + id + '" class="cam cam_' + id + '" src="/getPicture?id=' + id + '" alt="" />');
		
		$('.cam_' + id)
			.load(function(response, status, xhr) {console.log('new cam pic loaded..');
				$('.cam.old-' + $(this).attr('id').split('_')[1]).css({width: $(this).width() + 'px', height: $(this).height() + 'px', position: 'absolute'}).animate({opacity: 0}, 50, function() {
					$(this).remove();
				});
				setTimeout(function() {
					refreshCam();
				}, 150);
			});
		setTimeout(function() {
			if(((new Date().getTime()) - id) > 900) {
				$('.cam_' + id).remove();
				console.log('restarting img refresher..##############');
				refreshCam();
			}
		}, 1000);
	};
	
	setTimeout(function() {
		refreshCam();
	}, 3000);
	
	$('*:not(input, textarea, .login_area, .login_area *, html, document, body, .container)').disableSelection();
	
	$('.simple_commands .box, .current_queue .box').sortable({
        connectWith: '.command_sortable',
        placeholder: false,
		forcePlaceholderSize: false,
      	dropOnEmpty: true,
        remove: function(event, ui) {
                	if($(this).parent().hasClass('simple_commands')) {
                		ui.item.clone().appendTo('.current_queue .box');
                	}
                	$(this).sortable('cancel');
                	set();
                },
        start: function(event, ui) {
        			if($(this).parent().hasClass('simple_commands')) {
        				if($('.simple_commands .box .item').index(ui.item) > 0) {
        					ui.item.clone().addClass('duplicate').removeAttr('style').insertAfter('.simple_commands .box .item:eq(' + ($('.simple_commands .box .item').index(ui.item)-1) + ')');
                		}
                		else {
                			ui.item.clone().addClass('duplicate').removeAttr('style').prependTo('.simple_commands .box');
                		}
                	}
        		},
        stop: function(event, ui) {
       				$('.simple_commands .box .duplicate').remove();
       				$(this).sortable('cancel'); 
       				set();
        		}
    });
	
	$('.current_queue .box').sortable({
		containment: $('.current_queue'),
		axis: 'y',
		placeholder: 'sortable-placeholder',
		helper: 'clone',
		revert: true,
		forceHelperSize: true,
		forcePlaceholderSize: true,
        remove: function(event, ui) {
                },
        start: function(event, ui) {
        			ui.item.addClass('invisible');
        		},
        stop: function(event, ui) {
        			ui.item.removeClass('invisible');
        		}
	});
	
	$('.login_form').submit(function() {
		$.post('/login', $(this).serialize(), function(data) {
			r = data.split('|');
			
			if(r[0] == 0) {
				error(r[1]);
			}
			else {
				info(r[1], 1);
			}
		});
		return false;
	});
	
	f0 = function() {
		$(".dialog").attr('title', 'System alert!').html('Do you really want to clear the queue?').dialog({
	        resizable: false,
	        modal: true,
	        show: 'puff',
	        hide: 'explode',
	    	buttons: {
	    		No: function() {
	    			$(this).dialog('close');
	    		},
	    		Yes: function() {
	    			$('.current_queue .item').animate({opacity: 0}, 1000, function() {
	    				$(this).remove();
	    			});
	    		  	$(this).dialog('close');
	    		}
	    	}
	    });
	};
	
	f1 = function() {
		$(".dialog").attr('title', 'Choose a name').html('Please enter a name for your new queue:<input name="queue_name" type="text" />').dialog({
	        resizable: false,
	        modal: true,
	        show: 'puff',
	        hide: 'explode',
	    	buttons: {
	    		Abort: function() {
	    		  	$(this).dialog('close');
	    		},
	    		Save: function() {
	    			$(this).dialog('close');
	    			
	    			items  = '';
	    			params = '';
	    			i = 0;
	    			
	    			sel = $('.current_queue .box .item');
	    			
	    			while(i < sel.length) {
	    				items = items + '|' + sel.eq(i).attr('data-id');
	    				psel = $('.params .param', sel.eq(i));
	    				
	    				ii = 0;
	    				
	    				xparams = '';
			
						while(ii < psel.length) {
							if($('select', psel.eq(ii)).length > 0) {
								xparams = xparams + ';' + $('select', psel.eq(ii)).find(":selected").attr('value').replace(/;/g, '.');
							}
							else if($('textarea', psel.eq(ii)).length > 0) {
								xparams = xparams + ';' + $('textarea', psel.eq(ii)).val().replace(/;/g, '.');
							}
							else {
								xparams = xparams + ';' + $('input', psel.eq(ii)).val().replace(/;/g, '.');
							}
							ii++;
						}
	    				
	    				params = params + '|' + xparams.substr(1);
	    				i++;
	    			}
	    			$.post('/saveQueue', 'queue_name=' + $('[name=queue_name]').val() + '&queue_items=' + items.substr(1) + '&queue_params=' + params.substr(1), function(data) {
	    				r = data.split('|');
						
	    				if(r[0] < 1) {
	    					if(r[0] == -1) {
	    						reload = 1;
	    					}
	    					else {
	    						reload = 0;
	    					}
	    					error(r[1], reload);
	    				}
	    				else {
	    					info(r[1]);
	    					$('.saved_queues .box').append(r[2]);
	    					set();
	    				}
	    			});
	    		}
	    	}
	    });
	};
	
	f2 = function() {
		$(".dialog").attr('title', 'System alert!').html('Do you really want to delete this queue?<input name="queue_id" type="hidden" value="' + $(this).parent().parent().attr('data-id') + '" />').dialog({
	        resizable: false,
	        modal: true,
	        show: 'puff',
	        hide: 'explode',
	    	buttons: {
	    		No: function() {
	    		  	$(this).dialog('close');
	    		},
	    		Yes: function() {
	    			$(this).dialog('close');
	    			
	    			console.log('queue_id=' + $('[name=queue_id]').val());
					
	    			$.post('/deleteQueue', 'queue_id=' + $('[name=queue_id]').val(), function(data) {
	    				r = data.split('|');
						
	    				if(r[0] < 1) {
	    					if(r[0] == -1) {
	    						reload = 1;
	    					}
	    					else {
	    						reload = 0;
	    					}
	    					error(r[1], reload);
	    				}
	    				else {
	    					info(r[1]);
	    					$('[data-id=' + $('[name=queue_id]').val() + ']').animate({opacity: 0}, function() {
	    						$(this).remove();
	    					});
	    				}
	    			});
	    		}
	    	}
	    });
	};
	
	f3 = function() {
		$(this).parent().parent().animate({height: '0px', opacity: 0}, function() {
		  $(this).remove();
		});
	};
	
	f4 = function() {
		if($('.params *', this).length > 0 && $(this).data('expanded') == 0) {
			$('.current_queue .item').not(this).trigger('mouseleave');
			h = $(this).css({height: 'auto'}).height();
			$(this).removeAttr('style').data('expanded', 1).animate({height: h + 'px'});
		}
	};
	
	f5 = function() {
		h = $(this).height();
		h2 = $(this).removeAttr('style').css({overflow: 'hidden'}).height();
		$(this).data('expanded', 0).css({height: h + 'px'}).animate({height: h2 + 'px'});
	};
	
	f6 = function() {
		$(this).parent().parent().parent().trigger('click');
	};
	
	f7 = function(event, master) {
		if($('.current_queue').data('started') == '1' && (typeof(master) == 'undefined' || master != 1)) {
			console.log('Queue in action! No additional operation-execution is allowed!');
			return false;
		}
		if(typeof($(this).data('uid')) == 'undefined') {
			$(this).data('uid', new Date().getTime());
		}
		params = Array();
		demo   = 0;
		
		if($('.params *', this).length > 0) {
			i = 0;
			p = $('.params .param', this);
			
			while(i < p.length) {
				if($('select', p.eq(i)).length > 0) {
					params.push($('select', p.eq(i)).find(":selected").attr('value'));
				}
				else if($('textarea', p.eq(i)).length > 0) {
					params.push($('textarea', p.eq(i)).val());
				}
				else {
					params.push($('input', p.eq(i)).val());
				}
				i++;
			}
		}
		if($(this).parent().parent().hasClass('simple_commands')) {
			demo = 1;
		}
		console.log(demo);
		console.log('exec..');
		$.post('/exec', {command: $(this).attr('data-id'), params: params.join('|'), uid: $(this).data('uid'), demo: demo}, function(data) {
			console.log(data);
			$('.current_queue').trigger('execNext');
			
			r = data.split('||');
			
			if(r[0] == 1) {
				$('.console .box').prepend(r[1]);
				set();
			}
			else {
				error(r[1]);
			}
		});
	};
	
	f8 = function() {
		$.post('/stop', {uid: $(this).data('uid')}, function(data) {
			console.log(data);
		});
	};
	
    $('.current_queue .box').droppable({
    	activeClass: 'drop-active',
    	hoverClass: 'drop-hover',
    	drop: function(event, ui) {
    		if(!$('.ui-draggable-dragging').hasClass('queue')) {
    			return false;
    		}
    		f = function() {
				$.get('/queueItems', 'id=' + id, function(data) {
					r = data.split('|');
					
					if(r[0] < 1) {
						if(r[0] == -1) {
							reload = 1;
						}
						else {
							reload = 0;
						}
						error(r[1], reload);
					}
					else {
						f = function() {
							$('.current_queue .box').html(r[2]);
							$('.current_queue .box .item').css({opacity: 0}).animate({opacity: 1});
							set();
							info(r[1]);
						};
						
						if($('.current_queue .box .item').length > 0) {
							$('.current_queue .box .item').animate({opacity: 0}, function() {
								f();
							});
						}
						else {
							f();
						}
					}
				});
    		};
    		id = $('.queue.ui-draggable-dragging').attr('data-id');
	    	$('.queue.ui-draggable-dragging').remove();
	    	
    		if($('.current_queue .box .item').length > 0) {
    			$(".dialog").attr('title', 'System alert!').html('Do you really want to load this queue?<br />The current unsaved queue will become lost.').dialog({
			        resizable: false,
			        modal: true,
			        show: 'puff',
			        hide: 'explode',
			    	buttons: {
			    		No: function() {
			    		  	$(this).dialog('close');
			    		},
			    		Yes: function() {
			    			$(this).dialog('close');
			    			f(id);
			    		}
			    	}
			    });
    		}
    	    else {
    	    	f(id);
    	    }
    	}
    });
	
	set = function() {
		$('.current_queue .clear').unbind('click', f0).bind('click', f0);
		$('.current_queue .save').unbind('click', f1).bind('click', f1);
		$('.saved_queues .delete').unbind('click', f2).bind('click', f2);
	    $('.current_queue .play').removeClass('play').addClass('delete').unbind('click', f3).bind('click', f3);
		$('.current_queue .item').data('expanded', 0).unbind('click', f4).bind('click', f4).unbind('mouseleave', f5).bind('mouseleave', f5);
		$('.current_queue input, .current_queue select').unbind('focus', f6).bind('focus', f6);
		$('.commands .item, .current_queue .item').unbind('exec', f7).bind('exec', f7).unbind('stopExec', f8).bind('stopExec', f8);
		
		$('.saved_queues .item').draggable({
			revert: 'invalid',
			helper: 'clone',
			start: function( event, ui ) {
					$('.queue.ui-draggable-dragging').css({width: $('.saved_queues .queue:eq(0)').width() + 'px'});
				}
		});
	};
	
	info = function(message, reload) {
		$('.system_alert').removeClass('error').addClass('info').html(message).css({opacity: 0, right: -$('.system_alert').width() + 'px'}).animate({opacity: 1, right: '20px'}, function() {
			setTimeout(function() {
				$('.system_alert').animate({opacity: 0}, 500);
			}, 2000);
			
			if(typeof(reload) != 'undefined' && reload == 1) {
			  setTimeout(function() {
					location.reload();
				}, 2500);
			}
		});
	};
	
	error = function(message, reload) {
		$('.system_alert').removeClass('info').addClass('error').html(message).css({opacity: 0, right: -$('.system_alert').width() + 'px'}).animate({opacity: 1, right: '20px'}, function() {
			setTimeout(function() {
				$('.system_alert').animate({opacity: 0}, 500);
			}, 2000);
			
			if(typeof(reload) != 'undefined' && reload == 1) {
			  setTimeout(function() {
					location.reload();
				}, 2500);
			}
		});
	};
	
	set();
	/*
	$.post('/exec', {command: 'c81e728d', params: 'forward|0.3', uid: '123', demo: '0'}, function(data) {
	  console.log(data);
	});*/
	
	$('.commands .item .play')
		.mousedown(function() {
			$(this).parent().parent().trigger('exec');
		})
		.mouseup(function() {
			$(this).parent().parent().trigger('stopExec');
		});
	
	$('.current_queue .start').click(function() {
		if($('.current_queue').data('started') == '0') {
			$(this).removeClass('start').addClass('stop');
			$('.current_queue .item').removeClass('started').removeClass('finished');
			$('.current_queue').data('lastItem', '0').data('started', '1').data('stop', '0').trigger('execNext');
		}
		else {
			$(this).removeClass('stop').addClass('start');
			$('.current_queue .item').removeClass('started').removeClass('finished');
			$('.current_queue').data('started', '0').data('stop', '1');
			$('.current_queue').data('lastItem').trigger('stopExec');
		}
	});
	
	$('.current_queue').data('started', '0').data('stop', '0').bind('execNext', function() {
		item = $('.item:not(.started,.finished):eq(0)', $(this));
		
		if(typeof($('.current_queue').data('lastItem')) != 'undefined' && $('.current_queue').data('lastItem') != 0) {
			$('.current_queue').data('lastItem').addClass('finished');
		}
		if(item.length == 0) {
			$('.current_queue .stop').removeClass('stop').addClass('start');
			$('.current_queue .item').removeClass('started').removeClass('finished');
			$('.current_queue').data('started', '0').data('stop', '1');
		}
		else if($('.current_queue').data('stop') == 0) {
			$('.current_queue').data('lastItem', item);
			
		  	item.addClass('started').trigger('exec', ['1']);
		}
	});
});