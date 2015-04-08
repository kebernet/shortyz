<!DOCTYPE html>
<html>

    <head>
        <title>Shortyz for Facebook</title>
        
        <!-- 
        userId: <%=session.getAttribute("user.id")%>
        serverName: <%=request.getServerName() %>
        -->
        <style type="text/css">
            body {
                margin: 0px;
                padding: 0px;
                text-align: center;
            }
            #loadingIndicator {
                width: 100%;
                height: 300px;
            }
            .android {
                font-family: Verdana;
                font-size: 12px;
                text-align: center;
            }
        </style>
    </head>

    <body id="body">
        <div class="android"><a href="http://www.kebernet.net/Home/projects/shortyz">Get Shortyz for Android!</a></div>
        <script type="text/javascript"><!--
            google_ad_client = "pub-3082613281475517";
            /* shortyz leader ad */
            google_ad_slot = "7670643750";
            google_ad_width = 728;
            google_ad_height = 90;
            //-->
        </script>
        
        <iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>
        <!-- Note: Include this div markup as a workaround for a known bug in this release on IE where you may get a "operation aborted" error -->
        <div id="FB_HiddenIFrameContainer" style="display:none; position:absolute; left:-100px; top:-100px; width:0px; height: 0px;"></div>

 <% if(request.getServerName().indexOf("localhost1") != -1 ) { %>
        <script src="http://static.ak.connect.facebook.com/js/api_lib/v0.4/FeatureLoader.js.php" type="text/javascript"></script>
        <script type="text/javascript">
          var api_key = '<%=request.getParameter("fb_sig_api_key")%>';
          var channel_path = '/';
          FB_RequireFeatures(["CanvasUtil", "Api", "Connect"], function(){
            FB.XdComm.Server.init("./static/xd_receiver.html");
            FB.FBDebug.isEnabled = true;
            FB.FBDebug.logLevel = 4;
            FB.CanvasClient.startTimerToSizeToContent();
            FB.Facebook.init(api_key, channel_path);

                // require user to login
                FB.Connect.requireSession(function(){
                    FB.FBDebug.logLevel=1;
                    FB.FBDebug.dump("Current user id is " + api.get_session().uid);
                });
          });
          
        </script>
<%}%>

<% if(session.getAttribute("user.id") != null) { %>
        <script type="text/javascript"
                src="http://pagead2.googlesyndication.com/pagead/show_ads.js" defer="true">
        </script>
        <table id="loadingIndicator" border="0">
            <tbody>
                <tr valign="middle">
                    <td align="center"><img src="static/throbber.gif" alt="Loading.."/></td>
                </tr>
        </table>

        <script src="shortyz/shortyz.nocache.js" type="text/javascript"></script>
<% } %>
    </body>
</html>