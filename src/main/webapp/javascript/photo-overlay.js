$(
  function(){
    $("li.photo a.thumb").click(function(event) {
      event.preventDefault();
      $('#overlay').remove();
      $('body').append("<div id=\"overlay\"><div id=\"overlay-background\"></div></div>")
      $.getJSON(this.href,function(data,textStatus){
        image = data.images.preview
        $._img({'class': 'preview', 'src': 'http://photos.williams-family.ca/photos/' + image.fileName})
        .appendTo('#overlay')
        .data('width', image.width)
        .data('height', image.height)
        .hide()
        .load(function(){
          el = $('#overlay');
          $(this).fit(el.width() - 220, el.height() - 20).show();
        });
      });
      next_photo = $(this).parent().next().children('a.thumb');
      if (next_photo.length) {
        $._a({'id': 'overlay-next', 'href': next_photo.attr('href')})
          ._img({'class': 'nav-thumb', 'src': next_photo.children('img').attr('src')})
          .a_()
          .appendTo('#overlay')
        .click(function(event) {
          $('li.photo a.thumb[href=\'' + $(this).attr('href') + '\']').click();
          event.preventDefault();
        });
      };
      prev_photo = $(this).parent().prev().children('a.thumb');
      if (prev_photo.length) {
        $._a({'id': 'overlay-prev', 'href': prev_photo.attr('href')})
          ._img({'class': 'nav-thumb', 'src': prev_photo.children('img').attr('src')})
        .a_()
        .appendTo('#overlay')
        .click(function(event) {
          $('li.photo a.thumb[href=\'' + $(this).attr('href') + '\']').click();
          event.preventDefault();
        });
      };
      $('#overlay').click(function(event) {
        $('#overlay').remove();
      });
      event.preventDefault();
    });
    $(window).resize(function(event){
      el = $('#overlay');
      $('img.preview').fit(el.width() - 220, el.height() - 20);
    });
  }
);
