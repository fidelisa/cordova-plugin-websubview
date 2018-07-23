 var exec = require("cordova/exec");
 module.exports = {
   isAvailable: function (callback) {
     var errorHandler = function errorHandler(error) {
       callback(false);
     };
     exec(callback, errorHandler, "WebSubView", "isAvailable", []);
   },
   load: function (options, onSuccess, onError) {
     options = options || {};
     if (!options.hasOwnProperty('animated')) {
       options.animated = true;
     }
     exec(onSuccess, onError, "WebSubView", "load", [options]);
   },
   show: function (tag, onSuccess, onError) {
     exec(onSuccess, onError, "WebSubView", "show", [tag]);
   },
   hide: function (tag, onSuccess, onError) {
     tag = tag || -1;
     exec(onSuccess, onError, "WebSubView", "hide", [tag]);
   },
   back: function (tag, onSuccess, onError) {
     tag = tag || -1;
     exec(onSuccess, onError, "WebSubView", "back", [tag]);
   },
   remove: function (tag, onSuccess, onError) {
     tag = tag || -1;
     exec(onSuccess, onError, "WebSubView", "remove", [tag]);
   },
   moveHorizontal: function (tag, options, onSuccess, onError) {
     tag = tag || -1;
     options = options || {};
     if (!options.hasOwnProperty('pixels')) {
       options.animated = 275;
     }

     if (!options.hasOwnProperty('duration')) {
       options.duration = 0.2;
     }

     exec(onSuccess, onError, "WebSubView", "moveHorizontal", [tag, options]);
   }

 };