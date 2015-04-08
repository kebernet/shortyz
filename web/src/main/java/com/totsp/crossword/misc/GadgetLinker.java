package com.totsp.crossword.misc;

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
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.EmittedArtifact;
import com.google.gwt.core.linker.XSLinker;
import com.google.gwt.dev.About;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Finalizes the module manifest file with the selection script.
 */
public final class GadgetLinker extends XSLinker {
    private EmittedArtifact manifestArtifact;

    @Override
    public String getDescription() {
        return "Google Gadget";
    }

    @Override
    public ArtifactSet link(TreeLogger logger, LinkerContext context,
        ArtifactSet artifacts) throws UnableToCompleteException {
        ArtifactSet toLink = new ArtifactSet(artifacts);

        // Mask the stub manifest created by the generator
        for (EmittedArtifact res : toLink.find(EmittedArtifact.class)) {
            if (res.getPartialPath().endsWith(".gadget.xml")) {
                manifestArtifact = res;
                toLink.remove(res);

                break;
            }
        }

        if (manifestArtifact == null) {
            if (artifacts.find(CompilationResult.class).isEmpty()) {
                // Maybe hosted mode or junit, defer to XSLinker.
                return new XSLinker().link(logger, context, toLink);
            } else {
                // When compiling for web mode, enforce that the manifest is present.
                logger.log(TreeLogger.ERROR,
                    "No gadget manifest found in ArtifactSet.");
                throw new UnableToCompleteException();
            }
        }

        return super.link(logger, context, toLink);
    }

    @Override
    protected String getSelectionScriptTemplate(TreeLogger logger,
        LinkerContext context) {
        return "com/totsp/crossword/misc/GadgetTemplate.js";
    }

    @Override
    protected EmittedArtifact emitSelectionScript(TreeLogger logger,
        LinkerContext context, ArtifactSet artifacts)
        throws UnableToCompleteException {
        logger = logger.branch(TreeLogger.DEBUG, "Building gadget manifest",
                null);

        String bootstrap = "<script>" +
            context.optimizeJavaScript(logger,
                generateSelectionScript(logger, context, artifacts)) +
            "</script>\n" + "<div id=\"__gwt_gadget_content_div\"></div>";

        // Read the content
        StringBuffer manifest = new StringBuffer();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                        manifestArtifact.getContents(logger)));

            for (String line = in.readLine(); line != null;
                    line = in.readLine()) {
                manifest.append(line).append("\n");
            }

            in.close();
        } catch (IOException e) {
            logger.log(TreeLogger.ERROR, "Unable to read manifest stub", e);
            throw new UnableToCompleteException();
        }

        replaceAll(manifest, "__BOOTSTRAP__", bootstrap);

        return emitString(logger, manifest.toString(),
            manifestArtifact.getPartialPath());
    }

    @Override
    protected String generateScriptInjector(String scriptUrl) {
        if (isRelativeURL(scriptUrl)) {
            return "  if (!__gwt_scriptsLoaded['" + scriptUrl + "']) {\n" +
            "    __gwt_scriptsLoaded['" + scriptUrl + "'] = true;\n" +
            "    document.write('<script language=\\\"javascript\\\" src=\\\"'+_IG_GetCachedUrl(base+'" +
            scriptUrl + "') + '\\\"></script>');\n" + "  }\n";
        } else {
            return "  if (!__gwt_scriptsLoaded['" + scriptUrl + "']) {\n" +
            "    __gwt_scriptsLoaded['" + scriptUrl + "'] = true;\n" +
            "    document.write('<script language=\\\"javascript\\\" src=\\\"'+_IG_GetCachedUrl('" +
            scriptUrl + "') + '\\\"></script>');\n" + "  }\n";
        }
    }

    @Override
    protected String generateSelectionScript(TreeLogger logger,
        LinkerContext context, ArtifactSet artifacts)
        throws UnableToCompleteException {
        StringBuffer scriptContents = new StringBuffer(super.generateSelectionScript(
                    logger, context, artifacts));

        // Add a substitution for the GWT major release number. e.g. "1.6"
        int[] gwtVersions = getVersionArray();
        replaceAll(scriptContents, "__GWT_MAJOR_VERSION__",
            gwtVersions[0] + "." + gwtVersions[1]);

        return scriptContents.toString();
    }

    @Override
    protected String generateStylesheetInjector(String stylesheetUrl) {
        if (isRelativeURL(stylesheetUrl)) {
            return "  if (!__gwt_stylesLoaded['" + stylesheetUrl + "']) {\n" +
            "    __gwt_stylesLoaded['" + stylesheetUrl + "'] = true;\n" +
            "    document.write('<link rel=\\\"stylesheet\\\" href=\\\"'+_IG_GetCachedUrl(base+'" +
            stylesheetUrl + "') + '\\\">');\n" + "  }\n";
        } else {
            return "  if (!__gwt_stylesLoaded['" + stylesheetUrl + "']) {\n" +
            "    __gwt_stylesLoaded['" + stylesheetUrl + "'] = true;\n" +
            "    document.write('<link rel=\\\"stylesheet\\\" href=\\\"'+_IG_GetCachedUrl('" +
            stylesheetUrl + "') + '\\\">');\n" + "  }\n";
        }
    }

    /**
     * TODO(zundel): remove this code once GWT 1.5 & 1.6 is obsolete and replace
     * with About.getGwtVersionArray()
     *
     * Workaround for issue 275 - wrong version in hosted mode. The static final
     * constants were being inlined into the gwt-gadgets jar file.
     *
     * @return version number as an array of 3 integers.
     */
    private static int[] getVersionArray() throws UnableToCompleteException {
        // Fails because in GWT 1.5, GWT_VERSION_NUMBER is static final and cached
        // result = About.GWT_VERSION_NUM.split("\\.");
        // GWT 2.0 has a new method for version parsing
        try {
            Method versionNumMethod = About.class.getMethod(
                    "getGwtVersionArray");

            return (int[]) versionNumMethod.invoke(null, (Object[]) null);
        } catch (NoSuchMethodException ex) {
            GWT.log("Couldn't fetch version from constant or method", ex);
        } catch (InvocationTargetException ex) {
            GWT.log("Couldn't fetch version from constant or method", ex);
        } catch (IllegalAccessException ex) {
            GWT.log("Couldn't fetch version from constant or method", ex);
        }

        // GWT 1.6 and prior has only a string constant
        try {
            Field versionNumField = About.class.getField("GWT_VERSION_NUM");
            String versionNumString = (String) versionNumField.get(null);
            String[] result = versionNumString.split("\\.");
            assert (result.length == 3);

            int[] val = {
                    Integer.valueOf(result[0]), Integer.valueOf(result[1]),
                    Integer.valueOf(result[2])
                };

            return val;
        } catch (NoSuchFieldException ex) {
            // No problem, just try another way to get the version
        } catch (IllegalAccessException ex) {
            GWT.log("Error trying to retrieve version", ex);

            // Fall through, there may be another way
        }

        throw new UnableToCompleteException();
    }
}
