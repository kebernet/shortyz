/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

function __MODULE_FUNC__() {
  // ---------------- INTERNAL GLOBALS ----------------

  // Cache symbols locally for good obfuscation
  var $wnd = window
  ,$doc = document

  // A variable to access functions in hosted mode
  ,external = $wnd.external

  // These variables gate calling gwtOnLoad; all must be true to start
  ,gwtOnLoad, bodyDone

  // If non-empty, an alternate base url for this module
  ,base = ''

  // A map of properties that were declared in meta tags
  ,metaProps = {}

  // Maps property names onto sets of legal values for that property.
  ,values = []

  // Maps property names onto a function to compute that property.
  ,providers = []

  // A multi-tier lookup map that uses actual property values to quickly find
  // the strong name of the cache.js file to load.
  ,answers = []

   // Provides the module with the soft permutation id
  ,softPermutationId = 0

  // Error functions.  Default unset in compiled mode, may be set by meta props.
  ,onLoadErrorFunc, propertyErrorFunc

  ,$stats = $wnd.__gwtStatsEvent ? function(a) {return $wnd.__gwtStatsEvent(a);} : null
  ; // end of global vars

  // ------------------ TRUE GLOBALS ------------------

  // Maps to synchronize the loading of styles and scripts; resources are loaded
  // only once, even when multiple modules depend on them.  This API must not
  // change across GWT versions.
  if (!$wnd.__gwt_stylesLoaded) { $wnd.__gwt_stylesLoaded = {}; }
  if (!$wnd.__gwt_scriptsLoaded) { $wnd.__gwt_scriptsLoaded = {}; }

  // --------------- INTERNAL FUNCTIONS ---------------

  function isHostedMode() {
    try {
      return (external && external.gwtOnLoad &&
          ($wnd.location.search.indexOf('gwt.hybrid') == -1));
    } catch (e) {
      // Defensive: some versions of IE7 reportedly can throw an exception
      // evaluating "external.gwtOnLoad".
      return false;
    }
  }

  // Called by onScriptLoad() and onload(). It causes
  // the specified module to be cranked up.
  //
  function maybeStartModule() {
    if (bodyDone) {
      if (isHostedMode()) {
        // Kicks off hosted mode
        try {
          external.gwtOnLoad($wnd, '__MODULE_NAME__', softPermutationId);
        } catch (e) {
          $wnd.alert("external.gwtOnLoad failed: " + e);
        };
      } else if (gwtOnLoad) {
        // Start the compiled permutation
        gwtOnLoad(onLoadErrorFunc, '__MODULE_NAME__', base, softPermutationId);
      }
    }
  }

  // Determine our own script's URL from the manifest's location
  // This function produces one side-effect, it sets base to the module's
  // base url.
  //
  function computeScriptBase() {
    // _args() is provided by the container
    base = _args()['url'];
    base = base.substring(0,  base.lastIndexOf('/') + 1);
  }

  // Called to slurp up all <meta> tags:
  // gwt:property, gwt:onPropertyErrorFn, gwt:onLoadErrorFn
  //
  function processMetas() {
    var meta;
    var prefs = new _IG_Prefs();

    if (meta = prefs.getString('gwt:onLoadErrorFn')) {
      try {
        onLoadErrorFunc = eval(meta);
      } catch (e) {
        alert('Bad handler \"' + content + '\" for \"gwt:onLoadErrorFn\"');
      }
    }

    if (meta = prefs.getString('gwt:onPropertyErrorFn')) {
      try {
        propertyErrorFunc = eval(meta);
      } catch (e) {
        alert('Bad handler \"' + content +
          '\" for \"gwt:onPropertyErrorFn\"');
      }
    }

    if (meta = prefs.getArray('gwt:property')) {
      for (var i = 0; i < meta.length; i++) {
        var content = meta[i];
        if (content) {
          var value, eq = content.indexOf('=');
          if (eq >= 0) {
            name = content.substring(0, eq);
            value = content.substring(eq+1);
          } else {
            name = content;
            value = '';
          }
          metaProps[name] = value;
        }
      }
    }
  }


  /**
   * Gadget iframe URLs are generated with the locale in the URL as a
   *  lang/country parameter pair (e.g. lang=en&country=US) in lieu of the
   *  single locale parameter.
   * ($wnd.__gwt_Locale is read by the property provider in I18N.gwt.xml)
   */
  function setLocale() {
    var args = $wnd.location.search;
    var lang = extractFromQueryStr(args, "lang");
    if (lang != null) {
      country = extractFromQueryStr(args, "country");
      if (country != null) {
            $wnd.__gwt_Locale = lang + "_" + country;
      } else {
        $wnd.__gwt_Locale = lang;
      }
    }
  }

  /**
   * Returns the value of a named parameter in the URL query string.
   */
  function extractFromQueryStr(args, argName) {
    var start = args.indexOf(argName + "=");
    if (start < 0) {
      return undefined;
    }
    var value = args.substring(start);
    var valueBegin = value.indexOf("=") + 1;
    var valueEnd = value.indexOf("&");
    if (valueEnd == -1) {
        valueEnd = value.length;
    }
    return value.substring(valueBegin, valueEnd);
  }

  /**
   * Determines whether or not a particular property value is allowed. Called by
   * property providers.
   *
   * @param propName the name of the property being checked
   * @param propValue the property value being tested
   */
  function __gwt_isKnownPropertyValue(propName, propValue) {
    return propValue in values[propName];
  }

  /**
   * Returns a meta property value, if any.  Used by DefaultPropertyProvider.
   */
  function __gwt_getMetaProperty(name) {
    var value = metaProps[name];
    return (value == null) ? null : value;
  }

  // Deferred-binding mapper function.  Sets a value into the several-level-deep
  // answers map. The keys are specified by a non-zero-length propValArray,
  // which should be a flat array target property values. Used by the generated
  // PERMUTATIONS code.
  //
  function unflattenKeylistIntoAnswers(propValArray, value) {
    var answer = answers;
    for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
      // lazy initialize an empty object for the current key if needed
      answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
    }
    // set the final one to the value
    answer[propValArray[n]] = value;
  }

  // Computes the value of a given property.  propName must be a valid property
  // name. Used by the generated PERMUTATIONS code.
  //
  function computePropValue(propName) {
    var value = providers[propName](), allowedValuesMap = values[propName];
    if (value in allowedValuesMap) {
      return value;
    }
    var allowedValuesList = [];
    for (var k in allowedValuesMap) {
      allowedValuesList[allowedValuesMap[k]] = k;
    }
    if (propertyErrorFunc) {
      propertyErrorFunc(propName, allowedValuesList, value);
    }
    throw null;
  }

  // --------------- PROPERTY PROVIDERS ---------------

// __PROPERTIES_BEGIN__
// __PROPERTIES_END__

  // --------------- EXPOSED FUNCTIONS ----------------

  // Called when the compiled script identified by moduleName is done loading.
  //
  __MODULE_FUNC__.onScriptLoad = function(gwtOnLoadFunc) {
    // Remove this whole function from the global namespace to allow GC
    __MODULE_FUNC__ = null;
    gwtOnLoad = gwtOnLoadFunc;
    maybeStartModule();
  }

  // --------------- STRAIGHT-LINE CODE ---------------

  // do it early for compile/browse rebasing
  computeScriptBase();
  processMetas();
  setLocale();

  // --------------- GADGET ONLOAD HOOK ---------------

  _IG_RegisterOnloadHandler(function() {
    if (!bodyDone) {
      bodyDone = true;
// __MODULE_STYLES_BEGIN__
     // Style resources are injected here to prevent operation aborted errors on ie
// __MODULE_STYLES_END__
      maybeStartModule();
    }
  });

  if (isHostedMode()) {
    // Set up the globals and execute the hosted mode hook function
    $wnd.$wnd = $wnd;
    $wnd.$doc = $doc;
    $wnd.$moduleName = '__MODULE_NAME__';
    $wnd.$moduleBase = base;
    $wnd.__gwt_getProperty = computePropValue;
    $wnd.__gwt_initHandlers = __MODULE_FUNC__.__gwt_initHandlers;
    $wnd.__gwt_module_id = 0;
    $wnd.fireOnModuleLoadStart = function(className) {
      $stats && $stats({moduleName:$moduleName, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date()).getTime(), type:'onModuleLoadStart', className:className});
    };
    $wnd.onunload = function() {
      external.gwtOnLoad($wnd, null, '__GWT_MAJOR_VERSION__');
    };
  } else {
    // Otherwise, inject the permutation
    var strongName;
    try {
// __PERMUTATIONS_BEGIN__
      // Permutation logic
// __PERMUTATIONS_END__
      var idx = strongName.indexOf(':');
      if (idx != -1) {
        softPermutationId = Number(strongName.substring(idx + 1));
        strongName = strongName.substring(0, idx);
      }
    } catch (e) {
      // intentionally silent on property failure
      return;
    }

// __MODULE_SCRIPTS_BEGIN__
  // Script resources are injected here
// __MODULE_SCRIPTS_END__

    // Use the container's caching function if it's available:
    var fullName = base + strongName;
    if (fullName.search("\.cache\.js$") < 0) {
        fullName = fullName.concat(".cache.js");
    }
    // Get a URL that is cached for a year (31536000 seconds).
    var loadFrom = _IG_GetCachedUrl(fullName, {refreshInterval:31536000});

    $doc.write('<script src="' + loadFrom + '"></script>');
  }
}

// Called from compiled code to hook the window's resize & load events (the
// code running in the script frame is not allowed to hook these directly).
//
// Notes:
// 1) We declare it here in the global scope so that it won't closure the
// internals of the module func.
//
// 2) We hang it off the module func to avoid polluting the global namespace.
//
// 3) This function will be copied directly into the script namespace.
//
__MODULE_FUNC__.__gwt_initHandlers = function(resize, beforeunload, unload) {
  var $wnd = window
  , oldOnResize = $wnd.onresize
  , oldOnBeforeUnload = $wnd.onbeforeunload
  , oldOnUnload = $wnd.onunload
  ;

  $wnd.onresize = function(evt) {
   try {
     resize();
   } finally {
     oldOnResize && oldOnResize(evt);
   }
  };

  $wnd.onbeforeunload = function(evt) {
    var ret, oldRet;
    try {
      ret = beforeunload();
    } finally {
      oldRet = oldOnBeforeUnload && oldOnBeforeUnload(evt);
    }
    // Avoid returning null as IE6 will coerce it into a string.
    // Ensure that "" gets returned properly.
    if (ret != null) {
          return ret;
        }
        if (oldRet != null) {
          return oldRet;
        }
   // returns undefined.
  };

  $wnd.onunload = function(evt) {
    try {
      unload();
    } finally {
      oldOnUnload && oldOnUnload(evt);
    }
  };
};

__MODULE_FUNC__();