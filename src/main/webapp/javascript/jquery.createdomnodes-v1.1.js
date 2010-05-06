/*
 * jQuery CreateNode Plugin (v1.1)
 * By Sylvain MATHIEU (www.sylvain-mathieu.com) for Zenexity (zenexity.fr)
 * MIT License (http://www.opensource.org/licenses/mit-license.php)
 */

jQuery.createDomNodes = {
    virtualBracket: 0,
    currentBracket: 0,
    starter: false,
    cond: true,
    basicTags: [
        "div","span","h1","h2","h3","h4","h5","h6","p","ul","li","a","br","img","strong","em",
        "form","label","input","select","option","textarea","button",
        "table","tr","th","td","dl","dt","dd"
    ],
    addTag: function() {
        var tagName = this;
        if(!jQuery.fn["_"+tagName+"_"]) {
            jQuery.fn["_"+tagName+"_"] = function(attrs) {
                return this._tag_(tagName, attrs);
            };
            jQuery.fn["_"+tagName] = function(attrs) {
                return this._tag(tagName, attrs);
            };
            jQuery.fn[tagName+"_"] = function() {
                return this.tag_();
            };
            jQuery["_"+tagName+"_"] = function(attrs) {
                jQuery.createDomNodes.starter = true;
                return jQuery(document.createElement(tagName)).applyAttrs(attrs);
            };
            jQuery["_"+tagName] = function(attrs) {
                jQuery.createDomNodes.starter = true;
                return jQuery(document.createElement(tagName)).applyAttrs(attrs);
            };
        }
    },
    createTags: function(tags) {
        jQuery(tags).each(jQuery.createDomNodes.addTag);
    }
};

jQuery.fn.applyAttrs = function(attrs) {
    return this.each(function() {
        var attrName;
        for(attrName in attrs) {
            jQuery(this).attr(attrName,attrs[attrName]);
        }
    });
};

jQuery.fn._tag_ = function(tagName, attrs, appendTag) {
    if(jQuery.createDomNodes.cond) {
        if(jQuery.createDomNodes.virtualBracket>jQuery.createDomNodes.currentBracket||jQuery.createDomNodes.starter) {
            jQuery.createDomNodes.starter = false;
            jQuery.createDomNodes.currentBracket = jQuery.createDomNodes.virtualBracket;
            return jQuery(document.createElement(tagName)).applyAttrs(attrs).appendTo(this);
        }
        else {
            jQuery.createDomNodes.currentBracket = jQuery.createDomNodes.virtualBracket;
            return jQuery(document.createElement(tagName)).applyAttrs(attrs).appendTo(this.parent());
        }
    }
    else {
        return this;
    }
};

jQuery.fn._tag = function(tagName, attrs) {
    if(jQuery.createDomNodes.cond) {
        var tmp = this._tag_(tagName, attrs);
        jQuery.createDomNodes.virtualBracket++;
        return tmp;
    }
    else {
        return this;
    }
};

jQuery.fn.tag_ = function() {
    if(jQuery.createDomNodes.cond) {
        jQuery.createDomNodes.virtualBracket--;
        return this.parent();
    }
    else {
        return this;
    }
};

jQuery.fn._append_ = function(text) {
    if(jQuery.createDomNodes.cond) {
        this.parent().append(text);
        return this;
    }
    else {
        return this;
    }
};

jQuery.fn._if = function(cond) {
    jQuery.createDomNodes.cond = cond;
    return this;
}

jQuery.fn.if_ = function() {
    jQuery.createDomNodes.cond = true;
    return this;
}

jQuery._if = function(cond) {
    jQuery.createDomNodes.cond = cond;
    return this;
}

jQuery.if_ = function() {
    jQuery.createDomNodes.cond = true;
    return this;
}

jQuery(document).ready(function(){
    jQuery.createDomNodes.createTags(jQuery.createDomNodes.basicTags);
});



