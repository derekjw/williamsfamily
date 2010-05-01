$(
  function(){
    $("li.photo a.thumb").click(function(event) {
      var next_photo, prev_photo;
      event.preventDefault();
      $('#overlay').remove();
      $('body').append('<div id="overlay"><div id="overlay-background"></div></div>');
      $.getJSON(this.href, function(data,textStatus){
        var size,image;
        size = $.map(data.images, function(v,i){if (v.size >= 720) {return v.size}}).sort()[0];
        image = $.map(data.images, function(v,i){if (v.size == size) {return v}})[0];
        $('<img class="preview" src="'+image.uri+'" />').appendTo('#overlay').
          data('width', image.width).data('height', image.height).hide().
          load(function(){
            var el = $('#overlay');
            $(this).fit(el.width() - 220, el.height() - 20).show();
          });
      });
      next_photo = $(this).parent().next().children('a.thumb');
      if (next_photo.length) {
        $('<a id="overlay-next" href="'+next_photo.attr('href')+'">'+
          '<img class="nav-thumb" src="'+next_photo.children('img').attr('src')+'" />'+
          '</a>').appendTo('#overlay')
          .click(function(event) {
            $("li.photo a.thumb[href='" + $(this).attr('href') + "']").click();
            event.preventDefault();});}
      prev_photo = $(this).parent().prev().children('a.thumb');
      if (prev_photo.length) {
        $('<a id="overlay-prev" href="'+prev_photo.attr('href')+'">'+
          '<img class="nav-thumb" src="'+prev_photo.children('img').attr('src')+'" />'+
          '</a>').appendTo('#overlay')
          .click(function(event) {
            $("li.photo a.thumb[href='" + $(this).attr('href') + "']").click();
            event.preventDefault();});}
      $('#overlay').click(function(event) {
        $('#overlay').remove();
      });
    });
    $(window).resize(function(event){
      var el = $('#overlay');
      $('img.preview').fit(el.width() - 220, el.height() - 20);
    });
  }
);
