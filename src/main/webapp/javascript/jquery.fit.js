jQuery.fn.fit = function(fitWidth, fitHeight) {
  return this.each(function() {
    var $this, w, h, scale_w, scale_h, scale;
    $this = $(this);
    w = $this.data('width');
    h = $this.data('height');
    if (w > 0 && h > 0) {
      scale_w = fitWidth / w;
      scale_h = fitHeight / h;
      scale = (scale_w < scale_h ? scale_w : scale_h);
      if (scale < 1) {
        $this.width(scale * w);
        $this.height(scale * h);
      } else {
        $this.width(w);
        $this.height(h);
      };
    };
  });
};
