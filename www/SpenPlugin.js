//****************************************************************************************
//Author Name       :   Marcus Manvinder
//Date              :   Sept-25-2012
//Purpose           :   This Java script file provides an interface to interact from HTML to Canvas.
//Table referred    :   NA
//Table updated     :   NA
//Most Important Related Files: org.apache.cordova.plugin.SCanvas.Java
//****************************************************************************************

var SCanvas = function() {
    
};

//********************************************************************************************************************
// Function Name    :   showCanvas
// Author Name      :   Marcus Manvinder
// Date             :   Sept-25-2012
// Input Parameters :   successCallback - The the success callback function.
//                      errorCallback - The error callback function.
// Purpose          :   The overridden function showCanvas.
// ********************************************************************************************************************
SCanvas.prototype.showCanvas = function(successCallback, errorCallback) {
    if (errorCallback == null) { errorCallback = function() {}}
   
    if (typeof errorCallback != "function")  {
        console.log("SCanvas.showCanvas failure: failure parameter not a function");
        return
    }
	if (successCallback == null) { successCallback = function() {}}
	
    if (typeof successCallback != "function") {
        console.log("SCanvas.showCanvas failure: success callback parameter must be a function");
        return
    }

    return cordova.exec(successCallback, errorCallback, 'SCanvas', 'showCanvas', []);
};

//********************************************************************************************************************
//Function Name    :   showCanvas
//Author Name      :   Marcus Manvinder
//Date             :   Sept-25-2012
//Input Parameters :   backgroundImageUrl - The String Url for the background image.
//                     saveOnlyForegroundImage - The boolean  saveOnlyForegroundImage specifies whether to save only forground or not.
//                     successCallback - The success callback function.
//                     errorCallback - The error callback function.
//Purpose          :   The overridden function showCanvas.
//********************************************************************************************************************
SCanvas.prototype.showCanvas = function(backgroundImageUrl, foregroundImageData, saveOnlyForegroundImage, successCallback, errorCallback) {
   	if (errorCallback == null) { errorCallback = function() {}}
   	
    if (typeof errorCallback != "function")  {
        console.log("SCanvas.showCanvas failure: failure parameter not a function");
        return
    }

    if (successCallback == null) { successCallback = function() {}}
    
    if (typeof successCallback != "function") {
        console.log("SCanvas.showCanvas failure: success callback parameter must be a function");
        return
    }

    return cordova.exec(successCallback, errorCallback, 'SCanvas', 'showCanvas', [{"backgroundImageUrl": backgroundImageUrl, "foregroundImageData": foregroundImageData, "saveOnlyForegroundImage": saveOnlyForegroundImage}]);
};

if(!window.plugins) {
    window.plugins = {};
}
if (!window.plugins.sCanvas) {
    window.plugins.sCanvas = new SCanvas();
}