@STATIC;1.0;p;11;CPWebView.jt;35429;@STATIC;1.0;i;8;CPView.ji;14;CPScrollView.jt;35378;objj_executeFile("CPView.j", YES);objj_executeFile("CPScrollView.j", YES);{var the_typedef = objj_allocateTypeDef("Window");
    objj_registerTypeDef(the_typedef);
}CPWebViewProgressStartedNotification = "CPWebViewProgressStartedNotification";
CPWebViewProgressFinishedNotification = "CPWebViewProgressFinishedNotification";
CPWebViewScrollAuto = 0;
CPWebViewScrollAppKit = 1;
CPWebViewScrollNative = 2;
CPWebViewScrollNone = 3;
CPWebViewAppKitScrollPollInterval = 1.0;
CPWebViewAppKitScrollMaxPollCount = 3;

{var the_class = objj_allocateClassPair(CPView, "CPWebView"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_scrollView", "CPScrollView"), new objj_ivar("_frameView", "CPView"), new objj_ivar("_iframe", "DOMElement"), new objj_ivar("_mainFrameURL", "CPString"), new objj_ivar("_backwardStack", "CPArray"), new objj_ivar("_forwardStack", "CPArray"), new objj_ivar("_ignoreLoadStart", "BOOL"), new objj_ivar("_ignoreLoadEnd", "BOOL"), new objj_ivar("_isLoading", "BOOL"), new objj_ivar("_downloadDelegate", "id"), new objj_ivar("_frameLoadDelegate", "id"), new objj_ivar("_policyDelegate", "id"), new objj_ivar("_resourceLoadDelegate", "id"), new objj_ivar("_UIDelegate", "id"), new objj_ivar("_wso", "CPWebScriptObject"), new objj_ivar("_url", "CPString"), new objj_ivar("_html", "CPString"), new objj_ivar("_loadCallback", "Function"), new objj_ivar("_scrollMode", "int"), new objj_ivar("_effectiveScrollMode", "int"), new objj_ivar("_contentIsAccessible", "BOOL"), new objj_ivar("_contentSizeCheckTimer", "CPTimer"), new objj_ivar("_contentSizePollCount", "int"), new objj_ivar("_loadHTMLStringTimer", "int"), new objj_ivar("_drawsBackground", "BOOL")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("initWithFrame:frameName:groupName:"), function $CPWebView__initWithFrame_frameName_groupName_(self, _cmd, frameRect, frameName, groupName)
        {
            if (self = (self == null ? null : (self.isa.method_msgSend["initWithFrame:"] || _objj_forward)(self, "initWithFrame:", frameRect)))
            {
                self._iframe.name = frameName;
            }
            return self;
        }

        ,["id","CGRect","CPString","CPString"]), new objj_method(sel_getUid("initWithFrame:"), function $CPWebView__initWithFrame_(self, _cmd, aFrame)
        {
            if (self = (objj_getClass("CPWebView").super_class.method_dtable["initWithFrame:"] || _objj_forward)(self, "initWithFrame:", aFrame))
            {
                self._mainFrameURL = nil;
                self._backwardStack = [];
                self._forwardStack = [];
                self._scrollMode = CPWebViewScrollAuto;
                self._contentIsAccessible = YES;
                self._isLoading = NO;
                self._drawsBackground = YES;
                (self == null ? null : (self.isa.method_msgSend["setBackgroundColor:"] || _objj_forward)(self, "setBackgroundColor:", (CPColor.isa.method_msgSend["whiteColor"] || _objj_forward)(CPColor, "whiteColor")));
                (self == null ? null : (self.isa.method_msgSend["_initDOMWithFrame:"] || _objj_forward)(self, "_initDOMWithFrame:", aFrame));
            }
            return self;
        }

        ,["id","CGRect"]), new objj_method(sel_getUid("_initDOMWithFrame:"), function $CPWebView___initDOMWithFrame_(self, _cmd, aFrame)
        {
            self._ignoreLoadStart = YES;
            self._ignoreLoadEnd = YES;
            self._iframe = document.createElement("iframe");
            self._iframe.name = "iframe_" + FLOOR(RAND() * 10000);
            self._iframe.style.width = "100%";
            self._iframe.style.height = "100%";
            self._iframe.style.borderWidth = "0px";
            self._iframe.frameBorder = "0";
            (self.isa.method_msgSend["_applyBackgroundColor"] || _objj_forward)(self, "_applyBackgroundColor");
            self._loadCallback =     function()
            {
                if (!self._ignoreLoadStart)
                {
                    (self.isa.method_msgSend["_startedLoading"] || _objj_forward)(self, "_startedLoading");
                    if (self._mainFrameURL)
                        ((___r1 = self._backwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["addObject:"] || _objj_forward)(___r1, "addObject:", self._mainFrameURL));
                    self._mainFrameURL = self._iframe.src;
                    ((___r1 = self._forwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["removeAllObjects"] || _objj_forward)(___r1, "removeAllObjects"));
                }        else
                    self._ignoreLoadStart = NO;
                if (!self._ignoreLoadEnd)
                {
                    (self.isa.method_msgSend["_finishedLoading"] || _objj_forward)(self, "_finishedLoading");
                }        else
                    self._ignoreLoadEnd = NO;
                ((___r1 = (CPRunLoop.isa.method_msgSend["currentRunLoop"] || _objj_forward)(CPRunLoop, "currentRunLoop")), ___r1 == null ? null : (___r1.isa.method_msgSend["limitDateForMode:"] || _objj_forward)(___r1, "limitDateForMode:", CPDefaultRunLoopMode));
                var ___r1;
            };
            if (self._iframe.addEventListener)
                self._iframe.addEventListener("load", self._loadCallback, false);
            else if (self._iframe.attachEvent)
                self._iframe.attachEvent("onload", self._loadCallback);
            self._frameView = ((___r1 = (CPView.isa.method_msgSend["alloc"] || _objj_forward)(CPView, "alloc")), ___r1 == null ? null : (___r1.isa.method_msgSend["initWithFrame:"] || _objj_forward)(___r1, "initWithFrame:", (self.isa.method_msgSend["bounds"] || _objj_forward)(self, "bounds")));
            ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setAutoresizingMask:"] || _objj_forward)(___r1, "setAutoresizingMask:", CPViewWidthSizable | CPViewHeightSizable));
            self._scrollView = ((___r1 = (CPScrollView.isa.method_msgSend["alloc"] || _objj_forward)(CPScrollView, "alloc")), ___r1 == null ? null : (___r1.isa.method_msgSend["initWithFrame:"] || _objj_forward)(___r1, "initWithFrame:", (self.isa.method_msgSend["bounds"] || _objj_forward)(self, "bounds")));
            ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setAutohidesScrollers:"] || _objj_forward)(___r1, "setAutohidesScrollers:", YES));
            ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setAutoresizingMask:"] || _objj_forward)(___r1, "setAutoresizingMask:", CPViewWidthSizable | CPViewHeightSizable));
            ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setDocumentView:"] || _objj_forward)(___r1, "setDocumentView:", self._frameView));
            self._frameView._DOMElement.appendChild(self._iframe);
            (self.isa.method_msgSend["_updateEffectiveScrollMode"] || _objj_forward)(self, "_updateEffectiveScrollMode");
            (self.isa.method_msgSend["addSubview:"] || _objj_forward)(self, "addSubview:", self._scrollView);
            var ___r1;
        }

        ,["id","CGRect"]), new objj_method(sel_getUid("setFrameSize:"), function $CPWebView__setFrameSize_(self, _cmd, aSize)
        {
            (objj_getClass("CPWebView").super_class.method_dtable["setFrameSize:"] || _objj_forward)(self, "setFrameSize:", aSize);
            (self.isa.method_msgSend["_resizeWebFrame"] || _objj_forward)(self, "_resizeWebFrame");
        }

        ,["void","CGSize"]), new objj_method(sel_getUid("viewDidUnhide"), function $CPWebView__viewDidUnhide(self, _cmd)
        {
            ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", ((___r2 = self._scrollView), ___r2 == null ? null : (___r2.isa.method_msgSend["contentSize"] || _objj_forward)(___r2, "contentSize"))));
            (self.isa.method_msgSend["_resizeWebFrame"] || _objj_forward)(self, "_resizeWebFrame");
            (self.isa.method_msgSend["_scheduleContentSizeCheck"] || _objj_forward)(self, "_scheduleContentSizeCheck");
            var ___r1, ___r2;
        }

        ,["void"]), new objj_method(sel_getUid("_attachScrollEventIfNecessary"), function $CPWebView___attachScrollEventIfNecessary(self, _cmd)
        {
            if (self._effectiveScrollMode !== CPWebViewScrollAppKit)
                return;
            var win = (self.isa.method_msgSend["DOMWindow"] || _objj_forward)(self, "DOMWindow");
            if (win && win.addEventListener)
            {
                var scrollEventHandler =         function(anEvent)
                {
                    var frameBounds = (self.isa.method_msgSend["bounds"] || _objj_forward)(self, "bounds"),
                        frameCenter = CGPointMake(CGRectGetMidX(frameBounds), CGRectGetMidY(frameBounds)),
                        windowOrigin = (self.isa.method_msgSend["convertPoint:toView:"] || _objj_forward)(self, "convertPoint:toView:", frameCenter, nil),
                        globalOrigin = ((___r1 = (self.isa.method_msgSend["window"] || _objj_forward)(self, "window")), ___r1 == null ? null : (___r1.isa.method_msgSend["convertBaseToBridge:"] || _objj_forward)(___r1, "convertBaseToBridge:", windowOrigin));
                    anEvent._overrideLocation = globalOrigin;
                    ((___r1 = ((___r2 = (self.isa.method_msgSend["window"] || _objj_forward)(self, "window")), ___r2 == null ? null : (___r2.isa.method_msgSend["platformWindow"] || _objj_forward)(___r2, "platformWindow"))), ___r1 == null ? null : (___r1.isa.method_msgSend["scrollEvent:"] || _objj_forward)(___r1, "scrollEvent:", anEvent));
                    var ___r1, ___r2;
                };
                win.addEventListener("DOMMouseScroll", scrollEventHandler, false);
                win.addEventListener("wheel", scrollEventHandler, false);
            }
        }

        ,["void"]), new objj_method(sel_getUid("_resizeWebFrame"), function $CPWebView___resizeWebFrame(self, _cmd)
        {
            if (!(self.isa.method_msgSend["_isVisible"] || _objj_forward)(self, "_isVisible"))
            {
                return;
            }
            if (self._effectiveScrollMode === CPWebViewScrollAppKit)
            {
                var visibleRect = ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["visibleRect"] || _objj_forward)(___r1, "visibleRect"));
                ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", CGSizeMake(CGRectGetMaxX(visibleRect), CGRectGetMaxY(visibleRect))));
                var win = (self.isa.method_msgSend["DOMWindow"] || _objj_forward)(self, "DOMWindow");
                if (win && win.document && win.document.body)
                {
                    var width = win.document.body.scrollWidth,
                        height = win.document.body.scrollHeight;
                    self._iframe.setAttribute("width", width);
                    self._iframe.setAttribute("height", height);
                    ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", CGSizeMake(width, height)));
                }
                else
                {
                    if (!win || !win.document)
                    {
                        CPLog.warn("using default size 800*1600");
                        ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", CGSizeMake(800, 1600)));
                    }
                }
                ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["scrollRectToVisible:"] || _objj_forward)(___r1, "scrollRectToVisible:", visibleRect));
            }
            var ___r1;
        }

        ,["void"]), new objj_method(sel_getUid("setScrollMode:"), function $CPWebView__setScrollMode_(self, _cmd, aScrollMode)
        {
            if (self._scrollMode == aScrollMode)
                return;
            self._scrollMode = aScrollMode;
            (self.isa.method_msgSend["_updateEffectiveScrollMode"] || _objj_forward)(self, "_updateEffectiveScrollMode");
        }

        ,["void","int"]), new objj_method(sel_getUid("effectiveScrollMode"), function $CPWebView__effectiveScrollMode(self, _cmd)
        {
            return self._effectiveScrollMode;
        }

        ,["int"]), new objj_method(sel_getUid("_updateEffectiveScrollMode"), function $CPWebView___updateEffectiveScrollMode(self, _cmd)
        {
            var _newScrollMode = CPWebViewScrollAppKit;
            if (self._scrollMode == CPWebViewScrollNative || self._scrollMode == CPWebViewScrollAuto && !self._contentIsAccessible || CPBrowserIsEngine(CPInternetExplorerBrowserEngine))
            {
                _newScrollMode = CPWebViewScrollNative;
            }
            else if (self._scrollMode == CPWebViewScrollAppKit && !self._contentIsAccessible)
            {
                CPLog.warn(self + " unable to use CPWebViewScrollAppKit scroll mode due to same origin policy.");
                _newScrollMode = CPWebViewScrollNative;
            }
            if (_newScrollMode !== self._effectiveScrollMode)
                (self.isa.method_msgSend["_setEffectiveScrollMode:"] || _objj_forward)(self, "_setEffectiveScrollMode:", _newScrollMode);
        }

        ,["void"]), new objj_method(sel_getUid("_setEffectiveScrollMode:"), function $CPWebView___setEffectiveScrollMode_(self, _cmd, aScrollMode)
        {
            self._effectiveScrollMode = aScrollMode;
            self._ignoreLoadStart = YES;
            self._ignoreLoadEnd = YES;
            var parent = self._iframe.parentNode;
            parent.removeChild(self._iframe);
            if (self._effectiveScrollMode === CPWebViewScrollAppKit)
            {
                ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setHasHorizontalScroller:"] || _objj_forward)(___r1, "setHasHorizontalScroller:", YES));
                ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setHasVerticalScroller:"] || _objj_forward)(___r1, "setHasVerticalScroller:", YES));
                self._iframe.setAttribute("scrolling", "no");
            }
            else if (self._effectiveScrollMode === CPWebViewScrollNone)
            {
                ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setHasHorizontalScroller:"] || _objj_forward)(___r1, "setHasHorizontalScroller:", NO));
                ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setHasVerticalScroller:"] || _objj_forward)(___r1, "setHasVerticalScroller:", NO));
                self._iframe.setAttribute("scrolling", "no");
            }
            else
            {
                ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setHasHorizontalScroller:"] || _objj_forward)(___r1, "setHasHorizontalScroller:", NO));
                ((___r1 = self._scrollView), ___r1 == null ? null : (___r1.isa.method_msgSend["setHasVerticalScroller:"] || _objj_forward)(___r1, "setHasVerticalScroller:", NO));
                self._iframe.setAttribute("scrolling", "auto");
                ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", ((___r2 = self._scrollView), ___r2 == null ? null : (___r2.isa.method_msgSend["bounds"] || _objj_forward)(___r2, "bounds")).size));
            }
            parent.appendChild(self._iframe);
            (self.isa.method_msgSend["_applyBackgroundColor"] || _objj_forward)(self, "_applyBackgroundColor");
            (self.isa.method_msgSend["_resizeWebFrame"] || _objj_forward)(self, "_resizeWebFrame");
            var ___r1, ___r2;
        }

        ,["void","int"]), new objj_method(sel_getUid("_maybePollWebFrameSize"), function $CPWebView___maybePollWebFrameSize(self, _cmd)
        {
            if (CPWebViewAppKitScrollMaxPollCount == 0 || self._contentSizePollCount++ < CPWebViewAppKitScrollMaxPollCount)
                (self.isa.method_msgSend["_resizeWebFrame"] || _objj_forward)(self, "_resizeWebFrame");
            else
                ((___r1 = self._contentSizeCheckTimer), ___r1 == null ? null : (___r1.isa.method_msgSend["invalidate"] || _objj_forward)(___r1, "invalidate"));
            var ___r1;
        }

        ,["void"]), new objj_method(sel_getUid("loadHTMLString:"), function $CPWebView__loadHTMLString_(self, _cmd, aString)
        {
            (self.isa.method_msgSend["loadHTMLString:baseURL:"] || _objj_forward)(self, "loadHTMLString:baseURL:", aString, nil);
        }

        ,["void","CPString"]), new objj_method(sel_getUid("loadHTMLString:baseURL:"), function $CPWebView__loadHTMLString_baseURL_(self, _cmd, aString, URL)
        {
            ((___r1 = self._frameView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", ((___r2 = self._scrollView), ___r2 == null ? null : (___r2.isa.method_msgSend["contentSize"] || _objj_forward)(___r2, "contentSize"))));
            (self.isa.method_msgSend["_startedLoading"] || _objj_forward)(self, "_startedLoading");
            self._ignoreLoadStart = YES;
            self._url = nil;
            self._html = aString;
            (self.isa.method_msgSend["_load"] || _objj_forward)(self, "_load");
            var ___r1, ___r2;
        }

        ,["void","CPString","CPURL"]), new objj_method(sel_getUid("_loadMainFrameURL"), function $CPWebView___loadMainFrameURL(self, _cmd)
        {
            (self.isa.method_msgSend["_startedLoading"] || _objj_forward)(self, "_startedLoading");
            self._ignoreLoadStart = YES;
            self._url = self._mainFrameURL;
            self._html = nil;
            (self.isa.method_msgSend["_load"] || _objj_forward)(self, "_load");
        }

        ,["void"]), new objj_method(sel_getUid("_load"), function $CPWebView___load(self, _cmd)
        {
            if (self._url)
            {
                var cpurl = (CPURL.isa.method_msgSend["URLWithString:"] || _objj_forward)(CPURL, "URLWithString:", self._url);
                self._contentIsAccessible = (cpurl == null ? null : (cpurl.isa.method_msgSend["_passesSameOriginPolicy"] || _objj_forward)(cpurl, "_passesSameOriginPolicy"));
                (self.isa.method_msgSend["_updateEffectiveScrollMode"] || _objj_forward)(self, "_updateEffectiveScrollMode");
                self._ignoreLoadEnd = NO;
                self._iframe.src = self._url;
            }
            else if (self._html !== nil)
            {
                self._iframe.src = "";
                self._contentIsAccessible = YES;
                (self.isa.method_msgSend["_updateEffectiveScrollMode"] || _objj_forward)(self, "_updateEffectiveScrollMode");
                self._ignoreLoadEnd = NO;
                if (self._loadHTMLStringTimer !== nil)
                {
                    window.clearTimeout(self._loadHTMLStringTimer);
                    self._loadHTMLStringTimer = nil;
                }
                self._loadHTMLStringTimer = window.setTimeout(        function()
                {
                    var win = (self.isa.method_msgSend["DOMWindow"] || _objj_forward)(self, "DOMWindow");
                    if (win)
                        win.document.write(self._html || "<html><body></body></html>");
                    window.setTimeout(self._loadCallback, 1);
                }, 0);
            }
        }

        ,["void"]), new objj_method(sel_getUid("_startedLoading"), function $CPWebView___startedLoading(self, _cmd)
        {
            self._isLoading = YES;
            ((___r1 = (CPNotificationCenter.isa.method_msgSend["defaultCenter"] || _objj_forward)(CPNotificationCenter, "defaultCenter")), ___r1 == null ? null : (___r1.isa.method_msgSend["postNotificationName:object:"] || _objj_forward)(___r1, "postNotificationName:object:", CPWebViewProgressStartedNotification, self));
            if (((___r1 = self._frameLoadDelegate), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("webView:didStartProvisionalLoadForFrame:"))))
                ((___r1 = self._frameLoadDelegate), ___r1 == null ? null : (___r1.isa.method_msgSend["webView:didStartProvisionalLoadForFrame:"] || _objj_forward)(___r1, "webView:didStartProvisionalLoadForFrame:", self, nil));
            var ___r1;
        }

        ,["void"]), new objj_method(sel_getUid("_finishedLoading"), function $CPWebView___finishedLoading(self, _cmd)
        {
            self._isLoading = NO;
            (self.isa.method_msgSend["_resizeWebFrame"] || _objj_forward)(self, "_resizeWebFrame");
            (self.isa.method_msgSend["_attachScrollEventIfNecessary"] || _objj_forward)(self, "_attachScrollEventIfNecessary");
            (self.isa.method_msgSend["_scheduleContentSizeCheck"] || _objj_forward)(self, "_scheduleContentSizeCheck");
            ((___r1 = (CPNotificationCenter.isa.method_msgSend["defaultCenter"] || _objj_forward)(CPNotificationCenter, "defaultCenter")), ___r1 == null ? null : (___r1.isa.method_msgSend["postNotificationName:object:"] || _objj_forward)(___r1, "postNotificationName:object:", CPWebViewProgressFinishedNotification, self));
            if (((___r1 = self._frameLoadDelegate), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("webView:didFinishLoadForFrame:"))))
                ((___r1 = self._frameLoadDelegate), ___r1 == null ? null : (___r1.isa.method_msgSend["webView:didFinishLoadForFrame:"] || _objj_forward)(___r1, "webView:didFinishLoadForFrame:", self, nil));
            var ___r1;
        }

        ,["void"]), new objj_method(sel_getUid("_scheduleContentSizeCheck"), function $CPWebView___scheduleContentSizeCheck(self, _cmd)
        {
            ((___r1 = self._contentSizeCheckTimer), ___r1 == null ? null : (___r1.isa.method_msgSend["invalidate"] || _objj_forward)(___r1, "invalidate"));
            if (self._effectiveScrollMode == CPWebViewScrollAppKit)
            {
                self._contentSizePollCount = 0;
                self._contentSizeCheckTimer = (CPTimer.isa.method_msgSend["scheduledTimerWithTimeInterval:target:selector:userInfo:repeats:"] || _objj_forward)(CPTimer, "scheduledTimerWithTimeInterval:target:selector:userInfo:repeats:", CPWebViewAppKitScrollPollInterval, self, sel_getUid("_maybePollWebFrameSize"), nil, YES);
            }
            var ___r1;
        }

        ,["void"]), new objj_method(sel_getUid("isLoading"), function $CPWebView__isLoading(self, _cmd)
        {
            return self._isLoading;
        }

        ,["BOOL"]), new objj_method(sel_getUid("mainFrameURL"), function $CPWebView__mainFrameURL(self, _cmd)
        {
            return self._mainFrameURL;
        }

        ,["CPString"]), new objj_method(sel_getUid("setMainFrameURL:"), function $CPWebView__setMainFrameURL_(self, _cmd, URLString)
        {
            if (self._mainFrameURL)
                ((___r1 = self._backwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["addObject:"] || _objj_forward)(___r1, "addObject:", self._mainFrameURL));
            self._mainFrameURL = URLString;
            ((___r1 = self._forwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["removeAllObjects"] || _objj_forward)(___r1, "removeAllObjects"));
            (self.isa.method_msgSend["_loadMainFrameURL"] || _objj_forward)(self, "_loadMainFrameURL");
            var ___r1;
        }

        ,["void","CPString"]), new objj_method(sel_getUid("goBack"), function $CPWebView__goBack(self, _cmd)
        {
            if (self._backwardStack.length > 0)
            {
                if (self._mainFrameURL)
                    ((___r1 = self._forwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["addObject:"] || _objj_forward)(___r1, "addObject:", self._mainFrameURL));
                self._mainFrameURL = ((___r1 = self._backwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["lastObject"] || _objj_forward)(___r1, "lastObject"));
                ((___r1 = self._backwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["removeLastObject"] || _objj_forward)(___r1, "removeLastObject"));
                (self.isa.method_msgSend["_loadMainFrameURL"] || _objj_forward)(self, "_loadMainFrameURL");
                return YES;
            }
            return NO;
            var ___r1;
        }

        ,["BOOL"]), new objj_method(sel_getUid("goForward"), function $CPWebView__goForward(self, _cmd)
        {
            if (self._forwardStack.length > 0)
            {
                if (self._mainFrameURL)
                    ((___r1 = self._backwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["addObject:"] || _objj_forward)(___r1, "addObject:", self._mainFrameURL));
                self._mainFrameURL = ((___r1 = self._forwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["lastObject"] || _objj_forward)(___r1, "lastObject"));
                ((___r1 = self._forwardStack), ___r1 == null ? null : (___r1.isa.method_msgSend["removeLastObject"] || _objj_forward)(___r1, "removeLastObject"));
                (self.isa.method_msgSend["_loadMainFrameURL"] || _objj_forward)(self, "_loadMainFrameURL");
                return YES;
            }
            return NO;
            var ___r1;
        }

        ,["BOOL"]), new objj_method(sel_getUid("canGoBack"), function $CPWebView__canGoBack(self, _cmd)
        {
            return self._backwardStack.length > 0;
        }

        ,["BOOL"]), new objj_method(sel_getUid("canGoForward"), function $CPWebView__canGoForward(self, _cmd)
        {
            return self._forwardStack.length > 0;
        }

        ,["BOOL"]), new objj_method(sel_getUid("backForwardList"), function $CPWebView__backForwardList(self, _cmd)
        {
            return {back: self._backwardStack, forward: self._forwardStack};
        }

        ,["WebBackForwardList"]), new objj_method(sel_getUid("close"), function $CPWebView__close(self, _cmd)
        {
            self._iframe.parentNode.removeChild(self._iframe);
        }

        ,["void"]), new objj_method(sel_getUid("DOMWindow"), function $CPWebView__DOMWindow(self, _cmd)
        {
            try {
                return self._iframe.contentDocument && self._iframe.contentDocument.defaultView || self._iframe.contentWindow;
            }
            catch(e) {
                return nil;
            }
        }

        ,["DOMWindow"]), new objj_method(sel_getUid("windowScriptObject"), function $CPWebView__windowScriptObject(self, _cmd)
        {
            var win = (self.isa.method_msgSend["DOMWindow"] || _objj_forward)(self, "DOMWindow");
            if (!self._wso || win != ((___r1 = self._wso), ___r1 == null ? null : (___r1.isa.method_msgSend["window"] || _objj_forward)(___r1, "window")))
            {
                if (win)
                    self._wso = ((___r1 = (CPWebScriptObject == null ? null : (CPWebScriptObject.isa.method_msgSend["alloc"] || _objj_forward)(CPWebScriptObject, "alloc"))), ___r1 == null ? null : (___r1.isa.method_msgSend["initWithWindow:"] || _objj_forward)(___r1, "initWithWindow:", win));
                else
                    self._wso = nil;
            }
            return self._wso;
            var ___r1;
        }

        ,["CPWebScriptObject"]), new objj_method(sel_getUid("stringByEvaluatingJavaScriptFromString:"), function $CPWebView__stringByEvaluatingJavaScriptFromString_(self, _cmd, script)
        {
            var result = (self.isa.method_msgSend["objectByEvaluatingJavaScriptFromString:"] || _objj_forward)(self, "objectByEvaluatingJavaScriptFromString:", script);
            return result ? String(result) : nil;
        }

        ,["CPString","CPString"]), new objj_method(sel_getUid("objectByEvaluatingJavaScriptFromString:"), function $CPWebView__objectByEvaluatingJavaScriptFromString_(self, _cmd, script)
        {
            return ((___r1 = (self.isa.method_msgSend["windowScriptObject"] || _objj_forward)(self, "windowScriptObject")), ___r1 == null ? null : (___r1.isa.method_msgSend["evaluateWebScript:"] || _objj_forward)(___r1, "evaluateWebScript:", script));
            var ___r1;
        }

        ,["JSObject","CPString"]), new objj_method(sel_getUid("computedStyleForElement:pseudoElement:"), function $CPWebView__computedStyleForElement_pseudoElement_(self, _cmd, element, pseudoElement)
        {
            var win = ((___r1 = (self.isa.method_msgSend["windowScriptObject"] || _objj_forward)(self, "windowScriptObject")), ___r1 == null ? null : (___r1.isa.method_msgSend["window"] || _objj_forward)(___r1, "window"));
            if (win)
            {
                return win.document.defaultView.getComputedStyle(element, pseudoElement);
            }
            return nil;
            var ___r1;
        }

        ,["DOMCSSStyleDeclaration","DOMElement","CPString"]), new objj_method(sel_getUid("drawsBackground"), function $CPWebView__drawsBackground(self, _cmd)
        {
            return self._drawsBackground;
        }

        ,["BOOL"]), new objj_method(sel_getUid("setDrawsBackground:"), function $CPWebView__setDrawsBackground_(self, _cmd, drawsBackground)
        {
            if (drawsBackground == self._drawsBackground)
                return;
            self._drawsBackground = drawsBackground;
            (self.isa.method_msgSend["_applyBackgroundColor"] || _objj_forward)(self, "_applyBackgroundColor");
        }

        ,["void","BOOL"]), new objj_method(sel_getUid("setBackgroundColor:"), function $CPWebView__setBackgroundColor_(self, _cmd, aColor)
        {
            (objj_getClass("CPWebView").super_class.method_dtable["setBackgroundColor:"] || _objj_forward)(self, "setBackgroundColor:", aColor);
            (self.isa.method_msgSend["_applyBackgroundColor"] || _objj_forward)(self, "_applyBackgroundColor");
        }

        ,["void","CPColor"]), new objj_method(sel_getUid("_applyBackgroundColor"), function $CPWebView___applyBackgroundColor(self, _cmd)
        {
            if (self._iframe)
            {
                var bgColor = (self.isa.method_msgSend["backgroundColor"] || _objj_forward)(self, "backgroundColor") || (CPColor.isa.method_msgSend["whiteColor"] || _objj_forward)(CPColor, "whiteColor");
                self._iframe.allowtransparency = !self._drawsBackground;
                self._iframe.style.backgroundColor = self._drawsBackground ? (bgColor == null ? null : (bgColor.isa.method_msgSend["cssString"] || _objj_forward)(bgColor, "cssString")) : "transparent";
            }
        }

        ,["void"]), new objj_method(sel_getUid("takeStringURLFrom:"), function $CPWebView__takeStringURLFrom_(self, _cmd, sender)
        {
            (self.isa.method_msgSend["setMainFrameURL:"] || _objj_forward)(self, "setMainFrameURL:", (sender == null ? null : (sender.isa.method_msgSend["stringValue"] || _objj_forward)(sender, "stringValue")));
        }

        ,["void","id"]), new objj_method(sel_getUid("goBack:"), function $CPWebView__goBack_(self, _cmd, sender)
        {
            (self.isa.method_msgSend["goBack"] || _objj_forward)(self, "goBack");
        }

        ,["void","id"]), new objj_method(sel_getUid("goForward:"), function $CPWebView__goForward_(self, _cmd, sender)
        {
            (self.isa.method_msgSend["goForward"] || _objj_forward)(self, "goForward");
        }

        ,["void","id"]), new objj_method(sel_getUid("stopLoading:"), function $CPWebView__stopLoading_(self, _cmd, sender)
        {
        }

        ,["void","id"]), new objj_method(sel_getUid("reload:"), function $CPWebView__reload_(self, _cmd, sender)
        {
            if (!self._url && self._html !== nil)
                (self.isa.method_msgSend["loadHTMLString:"] || _objj_forward)(self, "loadHTMLString:", self._html);
            else
                (self.isa.method_msgSend["_loadMainFrameURL"] || _objj_forward)(self, "_loadMainFrameURL");
        }

        ,["void","id"]), new objj_method(sel_getUid("print:"), function $CPWebView__print_(self, _cmd, sender)
        {
            try {
                (self.isa.method_msgSend["DOMWindow"] || _objj_forward)(self, "DOMWindow").print();
            }
            catch(e) {
                alert('Please click the webpage and select "Print" from the "File" menu');
            }
        }

        ,["void","id"]), new objj_method(sel_getUid("downloadDelegate"), function $CPWebView__downloadDelegate(self, _cmd)
        {
            return self._downloadDelegate;
        }

        ,["id"]), new objj_method(sel_getUid("setDownloadDelegate:"), function $CPWebView__setDownloadDelegate_(self, _cmd, anObject)
        {
            self._downloadDelegate = anObject;
        }

        ,["void","id"]), new objj_method(sel_getUid("frameLoadDelegate"), function $CPWebView__frameLoadDelegate(self, _cmd)
        {
            return self._frameLoadDelegate;
        }

        ,["id"]), new objj_method(sel_getUid("setFrameLoadDelegate:"), function $CPWebView__setFrameLoadDelegate_(self, _cmd, anObject)
        {
            self._frameLoadDelegate = anObject;
        }

        ,["void","id"]), new objj_method(sel_getUid("policyDelegate"), function $CPWebView__policyDelegate(self, _cmd)
        {
            return self._policyDelegate;
        }

        ,["id"]), new objj_method(sel_getUid("setPolicyDelegate:"), function $CPWebView__setPolicyDelegate_(self, _cmd, anObject)
        {
            self._policyDelegate = anObject;
        }

        ,["void","id"]), new objj_method(sel_getUid("resourceLoadDelegate"), function $CPWebView__resourceLoadDelegate(self, _cmd)
        {
            return self._resourceLoadDelegate;
        }

        ,["id"]), new objj_method(sel_getUid("setResourceLoadDelegate:"), function $CPWebView__setResourceLoadDelegate_(self, _cmd, anObject)
        {
            self._resourceLoadDelegate = anObject;
        }

        ,["void","id"]), new objj_method(sel_getUid("UIDelegate"), function $CPWebView__UIDelegate(self, _cmd)
        {
            return self._UIDelegate;
        }

        ,["id"]), new objj_method(sel_getUid("setUIDelegate:"), function $CPWebView__setUIDelegate_(self, _cmd, anObject)
        {
            self._UIDelegate = anObject;
        }

        ,["void","id"])]);
}

{var the_class = objj_allocateClassPair(CPObject, "CPWebScriptObject"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_window", "Window")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("initWithWindow:"), function $CPWebScriptObject__initWithWindow_(self, _cmd, aWindow)
        {
            if (self = (objj_getClass("CPWebScriptObject").super_class.method_dtable["init"] || _objj_forward)(self, "init"))
            {
                self._window = aWindow;
            }
            return self;
        }

        ,["id","Window"]), new objj_method(sel_getUid("callWebScriptMethod:withArguments:"), function $CPWebScriptObject__callWebScriptMethod_withArguments_(self, _cmd, methodName, args)
        {
            if (typeof self._window[methodName] == "function")
            {
                try {
                    return self._window[methodName].apply(self._window, args);
                }
                catch(e) {
                }
            }
            return undefined;
        }

        ,["id","CPString","CPArray"]), new objj_method(sel_getUid("evaluateWebScript:"), function $CPWebScriptObject__evaluateWebScript_(self, _cmd, script)
        {
            try {
                return self._window.eval(script);
            }
            catch(e) {
            }
            return undefined;
        }

        ,["id","CPString"]), new objj_method(sel_getUid("window"), function $CPWebScriptObject__window(self, _cmd)
        {
            return self._window;
        }

        ,["Window"])]);
}
{
    var the_class = objj_getClass("CPWebView")
    if(!the_class) throw new SyntaxError("*** Could not find definition for class \"CPWebView\"");
    var meta_class = the_class.isa;class_addMethods(the_class, [new objj_method(sel_getUid("initWithCoder:"), function $CPWebView__initWithCoder_(self, _cmd, aCoder)
    {
        self = (objj_getClass("CPWebView").super_class.method_dtable["initWithCoder:"] || _objj_forward)(self, "initWithCoder:", aCoder);
        if (self)
        {
            self._mainFrameURL = nil;
            self._backwardStack = [];
            self._forwardStack = [];
            self._scrollMode = CPWebViewScrollAuto;
            (self == null ? null : (self.isa.method_msgSend["_initDOMWithFrame:"] || _objj_forward)(self, "_initDOMWithFrame:", (self == null ? null : (self.isa.method_msgSend["frame"] || _objj_forward)(self, "frame"))));
            if (!(self == null ? null : (self.isa.method_msgSend["backgroundColor"] || _objj_forward)(self, "backgroundColor")))
                (self == null ? null : (self.isa.method_msgSend["setBackgroundColor:"] || _objj_forward)(self, "setBackgroundColor:", (CPColor.isa.method_msgSend["whiteColor"] || _objj_forward)(CPColor, "whiteColor")));
            (self == null ? null : (self.isa.method_msgSend["_updateEffectiveScrollMode"] || _objj_forward)(self, "_updateEffectiveScrollMode"));
        }
        return self;
    }

    ,["id","CPCoder"]), new objj_method(sel_getUid("encodeWithCoder:"), function $CPWebView__encodeWithCoder_(self, _cmd, aCoder)
    {
        var actualSubviews = self._subviews;
        self._subviews = [];
        (objj_getClass("CPWebView").super_class.method_dtable["encodeWithCoder:"] || _objj_forward)(self, "encodeWithCoder:", aCoder);
        self._subviews = actualSubviews;
    }

    ,["void","CPCoder"])]);
}
{
    var the_class = objj_getClass("CPURL")
    if(!the_class) throw new SyntaxError("*** Could not find definition for class \"CPURL\"");
    var meta_class = the_class.isa;class_addMethods(the_class, [new objj_method(sel_getUid("_passesSameOriginPolicy"), function $CPURL___passesSameOriginPolicy(self, _cmd)
    {
        var documentURL = (CPURL.isa.method_msgSend["URLWithString:"] || _objj_forward)(CPURL, "URLWithString:", window.location.href);
        if ((documentURL == null ? null : (documentURL.isa.method_msgSend["isFileURL"] || _objj_forward)(documentURL, "isFileURL")) && CPFeatureIsCompatible(CPSOPDisabledFromFileURLs))
            return YES;
        if (!(self.isa.method_msgSend["scheme"] || _objj_forward)(self, "scheme") && !(self.isa.method_msgSend["host"] || _objj_forward)(self, "host") && !(self.isa.method_msgSend["port"] || _objj_forward)(self, "port"))
            return YES;
        return (documentURL == null ? null : (documentURL.isa.method_msgSend["scheme"] || _objj_forward)(documentURL, "scheme")) == (self.isa.method_msgSend["scheme"] || _objj_forward)(self, "scheme") && (documentURL == null ? null : (documentURL.isa.method_msgSend["host"] || _objj_forward)(documentURL, "host")) == (self.isa.method_msgSend["host"] || _objj_forward)(self, "host") && (documentURL == null ? null : (documentURL.isa.method_msgSend["port"] || _objj_forward)(documentURL, "port")) == (self.isa.method_msgSend["port"] || _objj_forward)(self, "port");
    }

    ,["BOOL"])]);
}
p;17;CPAccordionView.jt;22454;@STATIC;1.0;I;20;Foundation/CPArray.jI;21;Foundation/CPObject.jI;32;Foundation/CPKeyValueObserving.jI;23;Foundation/CPIndexSet.jI;21;Foundation/CPString.ji;8;CPView.ji;10;CPButton.jt;22265;objj_executeFile("Foundation/CPArray.j", NO);objj_executeFile("Foundation/CPObject.j", NO);objj_executeFile("Foundation/CPKeyValueObserving.j", NO);objj_executeFile("Foundation/CPIndexSet.j", NO);objj_executeFile("Foundation/CPString.j", NO);objj_executeFile("CPView.j", YES);objj_executeFile("CPButton.j", YES);
{var the_class = objj_allocateClassPair(CPObject, "CPAccordionViewItem"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_identifier", "CPString"), new objj_ivar("_view", "CPView"), new objj_ivar("_label", "CPString")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("identifier"), function $CPAccordionViewItem__identifier(self, _cmd)
        {
            return self._identifier;
        }

        ,["CPString"]), new objj_method(sel_getUid("setIdentifier:"), function $CPAccordionViewItem__setIdentifier_(self, _cmd, newValue)
        {
            self._identifier = newValue;
        }

        ,["void","CPString"]), new objj_method(sel_getUid("view"), function $CPAccordionViewItem__view(self, _cmd)
        {
            return self._view;
        }

        ,["CPView"]), new objj_method(sel_getUid("setView:"), function $CPAccordionViewItem__setView_(self, _cmd, newValue)
        {
            self._view = newValue;
        }

        ,["void","CPView"]), new objj_method(sel_getUid("label"), function $CPAccordionViewItem__label(self, _cmd)
        {
            return self._label;
        }

        ,["CPString"]), new objj_method(sel_getUid("setLabel:"), function $CPAccordionViewItem__setLabel_(self, _cmd, newValue)
        {
            self._label = newValue;
        }

        ,["void","CPString"]), new objj_method(sel_getUid("init"), function $CPAccordionViewItem__init(self, _cmd)
        {
            return (self.isa.method_msgSend["initWithIdentifier:"] || _objj_forward)(self, "initWithIdentifier:", "");
        }

        ,["id"]), new objj_method(sel_getUid("initWithIdentifier:"), function $CPAccordionViewItem__initWithIdentifier_(self, _cmd, anIdentifier)
        {
            self = (objj_getClass("CPAccordionViewItem").super_class.method_dtable["init"] || _objj_forward)(self, "init");
            if (self)
                (self == null ? null : (self.isa.method_msgSend["setIdentifier:"] || _objj_forward)(self, "setIdentifier:", anIdentifier));
            return self;
        }

        ,["id","CPString"])]);
}

{var the_class = objj_allocateClassPair(CPView, "CPAccordionView"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_dirtyItemIndex", "CPInteger"), new objj_ivar("_itemHeaderPrototype", "CPView"), new objj_ivar("_items", "CPMutableArray"), new objj_ivar("_itemViews", "CPMutableArray"), new objj_ivar("_expandedItemIndexes", "CPIndexSet")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("initWithFrame:"), function $CPAccordionView__initWithFrame_(self, _cmd, aFrame)
        {
            self = (objj_getClass("CPAccordionView").super_class.method_dtable["initWithFrame:"] || _objj_forward)(self, "initWithFrame:", aFrame);
            if (self)
            {
                self._items = [];
                self._itemViews = [];
                self._expandedItemIndexes = (CPIndexSet.isa.method_msgSend["indexSet"] || _objj_forward)(CPIndexSet, "indexSet");
                (self == null ? null : (self.isa.method_msgSend["setItemHeaderPrototype:"] || _objj_forward)(self, "setItemHeaderPrototype:", ((___r1 = (CPButton.isa.method_msgSend["alloc"] || _objj_forward)(CPButton, "alloc")), ___r1 == null ? null : (___r1.isa.method_msgSend["initWithFrame:"] || _objj_forward)(___r1, "initWithFrame:", CGRectMake(0.0, 0.0, 100.0, 24.0)))));
            }
            return self;
            var ___r1;
        }

        ,["id","CGRect"]), new objj_method(sel_getUid("setItemHeaderPrototype:"), function $CPAccordionView__setItemHeaderPrototype_(self, _cmd, aView)
        {
            self._itemHeaderPrototype = aView;
        }

        ,["void","CPView"]), new objj_method(sel_getUid("itemHeaderPrototype"), function $CPAccordionView__itemHeaderPrototype(self, _cmd)
        {
            return self._itemHeaderPrototype;
        }

        ,["CPView"]), new objj_method(sel_getUid("items"), function $CPAccordionView__items(self, _cmd)
        {
            return self._items;
        }

        ,["CPArray"]), new objj_method(sel_getUid("addItem:"), function $CPAccordionView__addItem_(self, _cmd, anItem)
        {
            (self.isa.method_msgSend["insertItem:atIndex:"] || _objj_forward)(self, "insertItem:atIndex:", anItem, self._items.length);
        }

        ,["void","CPAccordionViewItem"]), new objj_method(sel_getUid("insertItem:atIndex:"), function $CPAccordionView__insertItem_atIndex_(self, _cmd, anItem, anIndex)
        {
            ((___r1 = self._expandedItemIndexes), ___r1 == null ? null : (___r1.isa.method_msgSend["addIndex:"] || _objj_forward)(___r1, "addIndex:", anIndex));
            var itemView = ((___r1 = (_CPAccordionItemView == null ? null : (_CPAccordionItemView.isa.method_msgSend["alloc"] || _objj_forward)(_CPAccordionItemView, "alloc"))), ___r1 == null ? null : (___r1.isa.method_msgSend["initWithAccordionView:"] || _objj_forward)(___r1, "initWithAccordionView:", self));
            (itemView == null ? null : (itemView.isa.method_msgSend["setIndex:"] || _objj_forward)(itemView, "setIndex:", anIndex));
            (itemView == null ? null : (itemView.isa.method_msgSend["setLabel:"] || _objj_forward)(itemView, "setLabel:", (anItem == null ? null : (anItem.isa.method_msgSend["label"] || _objj_forward)(anItem, "label"))));
            (itemView == null ? null : (itemView.isa.method_msgSend["setContentView:"] || _objj_forward)(itemView, "setContentView:", (anItem == null ? null : (anItem.isa.method_msgSend["view"] || _objj_forward)(anItem, "view"))));
            (self.isa.method_msgSend["addSubview:"] || _objj_forward)(self, "addSubview:", itemView);
            ((___r1 = self._items), ___r1 == null ? null : (___r1.isa.method_msgSend["insertObject:atIndex:"] || _objj_forward)(___r1, "insertObject:atIndex:", anItem, anIndex));
            ((___r1 = self._itemViews), ___r1 == null ? null : (___r1.isa.method_msgSend["insertObject:atIndex:"] || _objj_forward)(___r1, "insertObject:atIndex:", itemView, anIndex));
            (self.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(self, "_invalidateItemsStartingAtIndex:", anIndex);
            (self.isa.method_msgSend["setNeedsLayout"] || _objj_forward)(self, "setNeedsLayout");
            var ___r1;
        }

        ,["void","CPAccordionViewItem","CPInteger"]), new objj_method(sel_getUid("removeItem:"), function $CPAccordionView__removeItem_(self, _cmd, anItem)
        {
            (self.isa.method_msgSend["removeItemAtIndex:"] || _objj_forward)(self, "removeItemAtIndex:", ((___r1 = self._items), ___r1 == null ? null : (___r1.isa.method_msgSend["indexOfObjectIdenticalTo:"] || _objj_forward)(___r1, "indexOfObjectIdenticalTo:", anItem)));
            var ___r1;
        }

        ,["void","CPAccordionViewItem"]), new objj_method(sel_getUid("removeItemAtIndex:"), function $CPAccordionView__removeItemAtIndex_(self, _cmd, anIndex)
        {
            ((___r1 = self._expandedItemIndexes), ___r1 == null ? null : (___r1.isa.method_msgSend["removeIndex:"] || _objj_forward)(___r1, "removeIndex:", anIndex));
            ((___r1 = self._itemViews[anIndex]), ___r1 == null ? null : (___r1.isa.method_msgSend["removeFromSuperview"] || _objj_forward)(___r1, "removeFromSuperview"));
            ((___r1 = self._items), ___r1 == null ? null : (___r1.isa.method_msgSend["removeObjectAtIndex:"] || _objj_forward)(___r1, "removeObjectAtIndex:", anIndex));
            ((___r1 = self._itemViews), ___r1 == null ? null : (___r1.isa.method_msgSend["removeObjectAtIndex:"] || _objj_forward)(___r1, "removeObjectAtIndex:", anIndex));
            (self.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(self, "_invalidateItemsStartingAtIndex:", anIndex);
            (self.isa.method_msgSend["setNeedsLayout"] || _objj_forward)(self, "setNeedsLayout");
            var ___r1;
        }

        ,["void","CPInteger"]), new objj_method(sel_getUid("removeAllItems"), function $CPAccordionView__removeAllItems(self, _cmd)
        {
            var count = self._items.length;
            while (count--)
                (self.isa.method_msgSend["removeItemAtIndex:"] || _objj_forward)(self, "removeItemAtIndex:", count);
        }

        ,["void"]), new objj_method(sel_getUid("expandItemAtIndex:"), function $CPAccordionView__expandItemAtIndex_(self, _cmd, anIndex)
        {
            if (!((___r1 = self._itemViews[anIndex]), ___r1 == null ? null : (___r1.isa.method_msgSend["isCollapsed"] || _objj_forward)(___r1, "isCollapsed")))
                return;
            ((___r1 = self._expandedItemIndexes), ___r1 == null ? null : (___r1.isa.method_msgSend["addIndex:"] || _objj_forward)(___r1, "addIndex:", anIndex));
            ((___r1 = self._itemViews[anIndex]), ___r1 == null ? null : (___r1.isa.method_msgSend["setCollapsed:"] || _objj_forward)(___r1, "setCollapsed:", NO));
            (self.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(self, "_invalidateItemsStartingAtIndex:", anIndex);
            var ___r1;
        }

        ,["void","CPInteger"]), new objj_method(sel_getUid("collapseItemAtIndex:"), function $CPAccordionView__collapseItemAtIndex_(self, _cmd, anIndex)
        {
            if (((___r1 = self._itemViews[anIndex]), ___r1 == null ? null : (___r1.isa.method_msgSend["isCollapsed"] || _objj_forward)(___r1, "isCollapsed")))
                return;
            ((___r1 = self._expandedItemIndexes), ___r1 == null ? null : (___r1.isa.method_msgSend["removeIndex:"] || _objj_forward)(___r1, "removeIndex:", anIndex));
            ((___r1 = self._itemViews[anIndex]), ___r1 == null ? null : (___r1.isa.method_msgSend["setCollapsed:"] || _objj_forward)(___r1, "setCollapsed:", YES));
            (self.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(self, "_invalidateItemsStartingAtIndex:", anIndex);
            var ___r1;
        }

        ,["void","CPInteger"]), new objj_method(sel_getUid("toggleItemAtIndex:"), function $CPAccordionView__toggleItemAtIndex_(self, _cmd, anIndex)
        {
            var itemView = self._itemViews[anIndex];
            if ((itemView == null ? null : (itemView.isa.method_msgSend["isCollapsed"] || _objj_forward)(itemView, "isCollapsed")))
                (self.isa.method_msgSend["expandItemAtIndex:"] || _objj_forward)(self, "expandItemAtIndex:", anIndex);
            else
                (self.isa.method_msgSend["collapseItemAtIndex:"] || _objj_forward)(self, "collapseItemAtIndex:", anIndex);
        }

        ,["void","CPInteger"]), new objj_method(sel_getUid("expandedItemIndexes"), function $CPAccordionView__expandedItemIndexes(self, _cmd)
        {
            return self._expandedItemIndexes;
        }

        ,["CPIndexSet"]), new objj_method(sel_getUid("collapsedItemIndexes"), function $CPAccordionView__collapsedItemIndexes(self, _cmd)
        {
            var indexSet = (CPIndexSet.isa.method_msgSend["indexSetWithIndexesInRange:"] || _objj_forward)(CPIndexSet, "indexSetWithIndexesInRange:", CPMakeRange(0, self._items.length));
            (indexSet == null ? null : (indexSet.isa.method_msgSend["removeIndexes:"] || _objj_forward)(indexSet, "removeIndexes:", self._expandedItemIndexes));
            return indexSet;
        }

        ,["CPIndexSet"]), new objj_method(sel_getUid("setEnabled:forItemAtIndex:"), function $CPAccordionView__setEnabled_forItemAtIndex_(self, _cmd, isEnabled, anIndex)
        {
            var itemView = self._itemViews[anIndex];
            if (!itemView)
                return;
            if (!isEnabled)
                (self.isa.method_msgSend["collapseItemAtIndex:"] || _objj_forward)(self, "collapseItemAtIndex:", anIndex);
            else
                (self.isa.method_msgSend["expandItemAtIndex:"] || _objj_forward)(self, "expandItemAtIndex:", anIndex);
            (itemView == null ? null : (itemView.isa.method_msgSend["setEnabled:"] || _objj_forward)(itemView, "setEnabled:", isEnabled));
        }

        ,["void","BOOL","CPInteger"]), new objj_method(sel_getUid("_invalidateItemsStartingAtIndex:"), function $CPAccordionView___invalidateItemsStartingAtIndex_(self, _cmd, anIndex)
        {
            if (self._dirtyItemIndex === CPNotFound)
                self._dirtyItemIndex = anIndex;
            self._dirtyItemIndex = MIN(self._dirtyItemIndex, anIndex);
            (self.isa.method_msgSend["setNeedsLayout"] || _objj_forward)(self, "setNeedsLayout");
        }

        ,["void","CPInteger"]), new objj_method(sel_getUid("setFrameSize:"), function $CPAccordionView__setFrameSize_(self, _cmd, aSize)
        {
            var width = CGRectGetWidth((self.isa.method_msgSend["frame"] || _objj_forward)(self, "frame"));
            (objj_getClass("CPAccordionView").super_class.method_dtable["setFrameSize:"] || _objj_forward)(self, "setFrameSize:", aSize);
            if (width !== CGRectGetWidth((self.isa.method_msgSend["frame"] || _objj_forward)(self, "frame")))
                (self.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(self, "_invalidateItemsStartingAtIndex:", 0);
        }

        ,["void","CGSize"]), new objj_method(sel_getUid("layoutSubviews"), function $CPAccordionView__layoutSubviews(self, _cmd)
        {
            if (self._items.length <= 0)
                return (self.isa.method_msgSend["setFrameSize:"] || _objj_forward)(self, "setFrameSize:", CGSizeMake(CGRectGetWidth((self.isa.method_msgSend["frame"] || _objj_forward)(self, "frame")), 0.0));
            if (self._dirtyItemIndex === CPNotFound)
                return;
            self._dirtyItemIndex = MIN(self._dirtyItemIndex, self._items.length - 1);
            var index = self._dirtyItemIndex,
                count = self._itemViews.length,
                width = CGRectGetWidth((self.isa.method_msgSend["bounds"] || _objj_forward)(self, "bounds")),
                y = index > 0 ? CGRectGetMaxY(((___r1 = self._itemViews[index - 1]), ___r1 == null ? null : (___r1.isa.method_msgSend["frame"] || _objj_forward)(___r1, "frame"))) : 0.0;
            self._dirtyItemIndex = CPNotFound;
            for (; index < count; ++index)
            {
                var itemView = self._itemViews[index];
                (itemView == null ? null : (itemView.isa.method_msgSend["setFrameY:width:"] || _objj_forward)(itemView, "setFrameY:width:", y, width));
                y = CGRectGetMaxY((itemView == null ? null : (itemView.isa.method_msgSend["frame"] || _objj_forward)(itemView, "frame")));
            }
            (self.isa.method_msgSend["setFrameSize:"] || _objj_forward)(self, "setFrameSize:", CGSizeMake(CGRectGetWidth((self.isa.method_msgSend["frame"] || _objj_forward)(self, "frame")), y));
            var ___r1;
        }

        ,["void"])]);
}

{var the_class = objj_allocateClassPair(CPView, "_CPAccordionItemView"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_accordionView", "CPAccordionView"), new objj_ivar("_isCollapsed", "BOOL"), new objj_ivar("_index", "CPInteger"), new objj_ivar("_headerView", "CPView"), new objj_ivar("_contentView", "CPView")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("isCollapsed"), function $_CPAccordionItemView__isCollapsed(self, _cmd)
        {
            return self._isCollapsed;
        }

        ,["BOOL"]), new objj_method(sel_getUid("setCollapsed:"), function $_CPAccordionItemView__setCollapsed_(self, _cmd, newValue)
        {
            self._isCollapsed = newValue;
        }

        ,["void","BOOL"]), new objj_method(sel_getUid("index"), function $_CPAccordionItemView__index(self, _cmd)
        {
            return self._index;
        }

        ,["CPInteger"]), new objj_method(sel_getUid("setIndex:"), function $_CPAccordionItemView__setIndex_(self, _cmd, newValue)
        {
            self._index = newValue;
        }

        ,["void","CPInteger"]), new objj_method(sel_getUid("initWithAccordionView:"), function $_CPAccordionItemView__initWithAccordionView_(self, _cmd, anAccordionView)
        {
            self = (objj_getClass("_CPAccordionItemView").super_class.method_dtable["initWithFrame:"] || _objj_forward)(self, "initWithFrame:", CGRectMakeZero());
            if (self)
            {
                self._accordionView = anAccordionView;
                self._isCollapsed = NO;
                var bounds = (self == null ? null : (self.isa.method_msgSend["bounds"] || _objj_forward)(self, "bounds"));
                self._headerView = (CPKeyedUnarchiver.isa.method_msgSend["unarchiveObjectWithData:"] || _objj_forward)(CPKeyedUnarchiver, "unarchiveObjectWithData:", (CPKeyedArchiver.isa.method_msgSend["archivedDataWithRootObject:"] || _objj_forward)(CPKeyedArchiver, "archivedDataWithRootObject:", ((___r1 = self._accordionView), ___r1 == null ? null : (___r1.isa.method_msgSend["itemHeaderPrototype"] || _objj_forward)(___r1, "itemHeaderPrototype"))));
                if (((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("setTarget:"))) && ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("setAction:"))))
                {
                    ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setTarget:"] || _objj_forward)(___r1, "setTarget:", self));
                    ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setAction:"] || _objj_forward)(___r1, "setAction:", sel_getUid("toggle:")));
                }
                (self == null ? null : (self.isa.method_msgSend["addSubview:"] || _objj_forward)(self, "addSubview:", self._headerView));
            }
            return self;
            var ___r1;
        }

        ,["id","CPAccordionView"]), new objj_method(sel_getUid("toggle:"), function $_CPAccordionItemView__toggle_(self, _cmd, aSender)
        {
            ((___r1 = self._accordionView), ___r1 == null ? null : (___r1.isa.method_msgSend["toggleItemAtIndex:"] || _objj_forward)(___r1, "toggleItemAtIndex:", (self.isa.method_msgSend["index"] || _objj_forward)(self, "index")));
            var ___r1;
        }

        ,["void","id"]), new objj_method(sel_getUid("setLabel:"), function $_CPAccordionItemView__setLabel_(self, _cmd, aLabel)
        {
            if (((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("setTitle:"))))
                ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setTitle:"] || _objj_forward)(___r1, "setTitle:", aLabel));
            else if (((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("setLabel:"))))
                ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setLabel:"] || _objj_forward)(___r1, "setLabel:", aLabel));
            else if (((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("setStringValue:"))))
                ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setStringValue:"] || _objj_forward)(___r1, "setStringValue:", aLabel));
            var ___r1;
        }

        ,["void","CPString"]), new objj_method(sel_getUid("setEnabled:"), function $_CPAccordionItemView__setEnabled_(self, _cmd, isEnabled)
        {
            if (((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["respondsToSelector:"] || _objj_forward)(___r1, "respondsToSelector:", sel_getUid("setEnabled:"))))
                ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setEnabled:"] || _objj_forward)(___r1, "setEnabled:", isEnabled));
            var ___r1;
        }

        ,["void","BOOL"]), new objj_method(sel_getUid("setContentView:"), function $_CPAccordionItemView__setContentView_(self, _cmd, aView)
        {
            if (self._contentView === aView)
                return;
            ((___r1 = self._contentView), ___r1 == null ? null : (___r1.isa.method_msgSend["removeObserver:forKeyPath:"] || _objj_forward)(___r1, "removeObserver:forKeyPath:", self, "frame"));
            ((___r1 = self._contentView), ___r1 == null ? null : (___r1.isa.method_msgSend["removeFromSuperview"] || _objj_forward)(___r1, "removeFromSuperview"));
            self._contentView = aView;
            ((___r1 = self._contentView), ___r1 == null ? null : (___r1.isa.method_msgSend["addObserver:forKeyPath:options:context:"] || _objj_forward)(___r1, "addObserver:forKeyPath:options:context:", self, "frame", CPKeyValueObservingOptionOld | CPKeyValueObservingOptionNew, NULL));
            (self.isa.method_msgSend["addSubview:"] || _objj_forward)(self, "addSubview:", self._contentView);
            ((___r1 = self._accordionView), ___r1 == null ? null : (___r1.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(___r1, "_invalidateItemsStartingAtIndex:", (self.isa.method_msgSend["index"] || _objj_forward)(self, "index")));
            var ___r1;
        }

        ,["void","CPView"]), new objj_method(sel_getUid("setFrameY:width:"), function $_CPAccordionItemView__setFrameY_width_(self, _cmd, aY, aWidth)
        {
            var headerHeight = CGRectGetHeight(((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["frame"] || _objj_forward)(___r1, "frame")));
            ((___r1 = self._headerView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", CGSizeMake(aWidth, headerHeight)));
            ((___r1 = self._contentView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameOrigin:"] || _objj_forward)(___r1, "setFrameOrigin:", CGPointMake(0.0, headerHeight)));
            if ((self.isa.method_msgSend["isCollapsed"] || _objj_forward)(self, "isCollapsed"))
                (self.isa.method_msgSend["setFrame:"] || _objj_forward)(self, "setFrame:", CGRectMake(0.0, aY, aWidth, headerHeight));
            else
            {
                var contentHeight = CGRectGetHeight(((___r1 = self._contentView), ___r1 == null ? null : (___r1.isa.method_msgSend["frame"] || _objj_forward)(___r1, "frame")));
                ((___r1 = self._contentView), ___r1 == null ? null : (___r1.isa.method_msgSend["setFrameSize:"] || _objj_forward)(___r1, "setFrameSize:", CGSizeMake(aWidth, contentHeight)));
                (self.isa.method_msgSend["setFrame:"] || _objj_forward)(self, "setFrame:", CGRectMake(0.0, aY, aWidth, contentHeight + headerHeight));
            }
            var ___r1;
        }

        ,["void","float","float"]), new objj_method(sel_getUid("resizeSubviewsWithOldSize:"), function $_CPAccordionItemView__resizeSubviewsWithOldSize_(self, _cmd, aSize)
        {
        }

        ,["void","CGSize"]), new objj_method(sel_getUid("observeValueForKeyPath:ofObject:change:context:"), function $_CPAccordionItemView__observeValueForKeyPath_ofObject_change_context_(self, _cmd, aKeyPath, anObject, aChange, aContext)
        {
            if (aKeyPath === "frame" && !CGRectEqualToRect((aChange == null ? null : (aChange.isa.method_msgSend["objectForKey:"] || _objj_forward)(aChange, "objectForKey:", CPKeyValueChangeOldKey)), (aChange == null ? null : (aChange.isa.method_msgSend["objectForKey:"] || _objj_forward)(aChange, "objectForKey:", CPKeyValueChangeNewKey))))
                ((___r1 = self._accordionView), ___r1 == null ? null : (___r1.isa.method_msgSend["_invalidateItemsStartingAtIndex:"] || _objj_forward)(___r1, "_invalidateItemsStartingAtIndex:", (self.isa.method_msgSend["index"] || _objj_forward)(self, "index")));
            var ___r1;
        }

        ,["void","CPString","id","CPDictionary","id"])]);
}
p;24;CPDictionaryController.jt;9946;@STATIC;1.0;i;19;CPArrayController.jt;9903;objj_executeFile("CPArrayController.j", YES);
{var the_class = objj_allocateClassPair(CPArrayController, "CPDictionaryController"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_contentDictionary", "CPDictionary"), new objj_ivar("_includedKeys", "CPArray"), new objj_ivar("_excludedKeys", "CPArray"), new objj_ivar("_initialKey", "CPString"), new objj_ivar("_initialValue", "id")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("includedKeys"), function $CPDictionaryController__includedKeys(self, _cmd)
        {
            return self._includedKeys;
        }

        ,["CPArray"]), new objj_method(sel_getUid("setIncludedKeys:"), function $CPDictionaryController__setIncludedKeys_(self, _cmd, newValue)
        {
            self._includedKeys = newValue;
        }

        ,["void","CPArray"]), new objj_method(sel_getUid("excludedKeys"), function $CPDictionaryController__excludedKeys(self, _cmd)
        {
            return self._excludedKeys;
        }

        ,["CPArray"]), new objj_method(sel_getUid("setExcludedKeys:"), function $CPDictionaryController__setExcludedKeys_(self, _cmd, newValue)
        {
            self._excludedKeys = newValue;
        }

        ,["void","CPArray"]), new objj_method(sel_getUid("initialKey"), function $CPDictionaryController__initialKey(self, _cmd)
        {
            return self._initialKey;
        }

        ,["CPString"]), new objj_method(sel_getUid("setInitialKey:"), function $CPDictionaryController__setInitialKey_(self, _cmd, newValue)
        {
            self._initialKey = newValue;
        }

        ,["void","CPString"]), new objj_method(sel_getUid("initialValue"), function $CPDictionaryController__initialValue(self, _cmd)
        {
            return self._initialValue;
        }

        ,["id"]), new objj_method(sel_getUid("setInitialValue:"), function $CPDictionaryController__setInitialValue_(self, _cmd, newValue)
        {
            self._initialValue = newValue;
        }

        ,["void","id"]), new objj_method(sel_getUid("init"), function $CPDictionaryController__init(self, _cmd)
        {
            self = (objj_getClass("CPDictionaryController").super_class.method_dtable["init"] || _objj_forward)(self, "init");
            if (self)
            {
                self._initialKey = "key";
                self._initialValue = "value";
            }
            return self;
        }

        ,["id"]), new objj_method(sel_getUid("newObject"), function $CPDictionaryController__newObject(self, _cmd)
        {
            var keys = ((___r1 = self._contentDictionary), ___r1 == null ? null : (___r1.isa.method_msgSend["allKeys"] || _objj_forward)(___r1, "allKeys")),
                newKey = self._initialKey,
                count = 0;
            if ((keys == null ? null : (keys.isa.method_msgSend["containsObject:"] || _objj_forward)(keys, "containsObject:", newKey)))
                while ((keys == null ? null : (keys.isa.method_msgSend["containsObject:"] || _objj_forward)(keys, "containsObject:", newKey)))
                    newKey = (CPString.isa.method_msgSend["stringWithFormat:"] || _objj_forward)(CPString, "stringWithFormat:", "%@%i", self._initialKey, ++count);
            return (self.isa.method_msgSend["_newObjectWithKey:value:"] || _objj_forward)(self, "_newObjectWithKey:value:", newKey, self._initialValue);
            var ___r1;
        }

        ,["id"]), new objj_method(sel_getUid("_newObjectWithKey:value:"), function $CPDictionaryController___newObjectWithKey_value_(self, _cmd, aKey, aValue)
        {
            var aNewObject = (_CPDictionaryControllerKeyValuePair == null ? null : (_CPDictionaryControllerKeyValuePair.isa.method_msgSend["new"] || _objj_forward)(_CPDictionaryControllerKeyValuePair, "new"));
            aNewObject._dictionary = self._contentDictionary;
            aNewObject._controller = self;
            aNewObject._key = aKey;
            if (aValue !== nil)
                (aNewObject == null ? null : (aNewObject.isa.method_msgSend["setValue:"] || _objj_forward)(aNewObject, "setValue:", aValue));
            return aNewObject;
        }

        ,["id","CPString","id"]), new objj_method(sel_getUid("contentDictionary"), function $CPDictionaryController__contentDictionary(self, _cmd)
        {
            return self._contentDictionary;
        }

        ,["CPDictionary"]), new objj_method(sel_getUid("setContentDictionary:"), function $CPDictionaryController__setContentDictionary_(self, _cmd, aDictionary)
        {
            if (aDictionary == self._contentDictionary)
                return;
            if ((aDictionary == null ? null : (aDictionary.isa.method_msgSend["isKindOfClass:"] || _objj_forward)(aDictionary, "isKindOfClass:", (CPDictionary.isa.method_msgSend["class"] || _objj_forward)(CPDictionary, "class"))))
                self._contentDictionary = aDictionary;
            else
                self._contentDictionary = nil;
            var array = (CPArray.isa.method_msgSend["array"] || _objj_forward)(CPArray, "array"),
                allKeys = ((___r1 = self._contentDictionary), ___r1 == null ? null : (___r1.isa.method_msgSend["allKeys"] || _objj_forward)(___r1, "allKeys"));
            (allKeys == null ? null : (allKeys.isa.method_msgSend["addObjectsFromArray:"] || _objj_forward)(allKeys, "addObjectsFromArray:", self._includedKeys));
            var iter = ((___r1 = (CPSet.isa.method_msgSend["setWithArray:"] || _objj_forward)(CPSet, "setWithArray:", allKeys)), ___r1 == null ? null : (___r1.isa.method_msgSend["objectEnumerator"] || _objj_forward)(___r1, "objectEnumerator")),
                obj;
            while ((obj = (iter == null ? null : (iter.isa.method_msgSend["nextObject"] || _objj_forward)(iter, "nextObject"))) !== nil)
                if (!((___r1 = self._excludedKeys), ___r1 == null ? null : (___r1.isa.method_msgSend["containsObject:"] || _objj_forward)(___r1, "containsObject:", obj)))
                    (array == null ? null : (array.isa.method_msgSend["addObject:"] || _objj_forward)(array, "addObject:", (self.isa.method_msgSend["_newObjectWithKey:value:"] || _objj_forward)(self, "_newObjectWithKey:value:", obj, nil)));
            (objj_getClass("CPDictionaryController").super_class.method_dtable["setContent:"] || _objj_forward)(self, "setContent:", array);
            var ___r1;
        }

        ,["void","CPDictionary"])]);
}
var CPIncludedKeys = "CPIncludedKeys",
    CPExcludedKeys = "CPExcludedKeys";
{
    var the_class = objj_getClass("CPDictionaryController")
    if(!the_class) throw new SyntaxError("*** Could not find definition for class \"CPDictionaryController\"");
    var meta_class = the_class.isa;class_addMethods(the_class, [new objj_method(sel_getUid("initWithCoder:"), function $CPDictionaryController__initWithCoder_(self, _cmd, aCoder)
    {
        self = (objj_getClass("CPDictionaryController").super_class.method_dtable["initWithCoder:"] || _objj_forward)(self, "initWithCoder:", aCoder);
        if (self)
        {
            self._includedKeys = (aCoder == null ? null : (aCoder.isa.method_msgSend["decodeObjectForKey:"] || _objj_forward)(aCoder, "decodeObjectForKey:", CPIncludedKeys));
            self._excludedKeys = (aCoder == null ? null : (aCoder.isa.method_msgSend["decodeObjectForKey:"] || _objj_forward)(aCoder, "decodeObjectForKey:", CPExcludedKeys));
            self._initialKey = "key";
            self._initialValue = "value";
        }
        return self;
    }

    ,["id","CPCoder"]), new objj_method(sel_getUid("encodeWithCoder:"), function $CPDictionaryController__encodeWithCoder_(self, _cmd, aCoder)
    {
        (objj_getClass("CPDictionaryController").super_class.method_dtable["encodeWithCoder:"] || _objj_forward)(self, "encodeWithCoder:", aCoder);
        (aCoder == null ? null : (aCoder.isa.method_msgSend["encodeObject:forKey:"] || _objj_forward)(aCoder, "encodeObject:forKey:", self._includedKeys, CPIncludedKeys));
        (aCoder == null ? null : (aCoder.isa.method_msgSend["encodeObject:forKey:"] || _objj_forward)(aCoder, "encodeObject:forKey:", self._excludedKeys, CPExcludedKeys));
    }

    ,["void","CPCoder"])]);
}

{var the_class = objj_allocateClassPair(CPObject, "_CPDictionaryControllerKeyValuePair"),
    meta_class = the_class.isa;class_addIvars(the_class, [new objj_ivar("_key", "CPString"), new objj_ivar("_dictionary", "CPDictionary"), new objj_ivar("_controller", "CPDictionaryController")]);objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("key"), function $_CPDictionaryControllerKeyValuePair__key(self, _cmd)
        {
            return self._key;
        }

        ,["CPString"]), new objj_method(sel_getUid("setKey:"), function $_CPDictionaryControllerKeyValuePair__setKey_(self, _cmd, newValue)
        {
            self._key = newValue;
        }

        ,["void","CPString"]), new objj_method(sel_getUid("dictionary"), function $_CPDictionaryControllerKeyValuePair__dictionary(self, _cmd)
        {
            return self._dictionary;
        }

        ,["CPDictionary"]), new objj_method(sel_getUid("setDictionary:"), function $_CPDictionaryControllerKeyValuePair__setDictionary_(self, _cmd, newValue)
        {
            self._dictionary = newValue;
        }

        ,["void","CPDictionary"]), new objj_method(sel_getUid("controller"), function $_CPDictionaryControllerKeyValuePair__controller(self, _cmd)
        {
            return self._controller;
        }

        ,["CPDictionaryController"]), new objj_method(sel_getUid("setController:"), function $_CPDictionaryControllerKeyValuePair__setController_(self, _cmd, newValue)
        {
            self._controller = newValue;
        }

        ,["void","CPDictionaryController"]), new objj_method(sel_getUid("value"), function $_CPDictionaryControllerKeyValuePair__value(self, _cmd)
        {
            return ((___r1 = self._dictionary), ___r1 == null ? null : (___r1.isa.method_msgSend["objectForKey:"] || _objj_forward)(___r1, "objectForKey:", self._key));
            var ___r1;
        }

        ,["id"]), new objj_method(sel_getUid("setValue:"), function $_CPDictionaryControllerKeyValuePair__setValue_(self, _cmd, aValue)
        {
            ((___r1 = self._dictionary), ___r1 == null ? null : (___r1.isa.method_msgSend["setObject:forKey:"] || _objj_forward)(___r1, "setObject:forKey:", aValue, self._key));
            var ___r1;
        }

        ,["void","id"]), new objj_method(sel_getUid("isExplicitlyIncluded"), function $_CPDictionaryControllerKeyValuePair__isExplicitlyIncluded(self, _cmd)
        {
            return ((___r1 = ((___r2 = self._controller), ___r2 == null ? null : (___r2.isa.method_msgSend["_includedKeys"] || _objj_forward)(___r2, "_includedKeys"))), ___r1 == null ? null : (___r1.isa.method_msgSend["containsObject:"] || _objj_forward)(___r1, "containsObject:", self._key));
            var ___r1, ___r2;
        }

        ,["BOOL"])]);
}
p;15;_CPCornerView.jt;3461;@STATIC;1.0;i;8;CPView.jt;3430;objj_executeFile("CPView.j", YES);
{var the_class = objj_allocateClassPair(CPView, "_CPCornerView"),
    meta_class = the_class.isa;objj_registerClassPair(the_class);
    class_addMethods(the_class, [new objj_method(sel_getUid("drawRect:"), function $_CPCornerView__drawRect_(self, _cmd, aRect)
        {
            var context = ((___r1 = (CPGraphicsContext.isa.method_msgSend["currentContext"] || _objj_forward)(CPGraphicsContext, "currentContext")), ___r1 == null ? null : (___r1.isa.method_msgSend["graphicsPort"] || _objj_forward)(___r1, "graphicsPort")),
                color = (self.isa.method_msgSend["currentValueForThemeAttribute:"] || _objj_forward)(self, "currentValueForThemeAttribute:", "divider-color");
            CGContextSetLineWidth(context, 1);
            CGContextSetStrokeColor(context, (self.isa.method_msgSend["currentValueForThemeAttribute:"] || _objj_forward)(self, "currentValueForThemeAttribute:", "divider-color"));
            CGContextMoveToPoint(context, CGRectGetMinX(aRect) + 0.5, ROUND(CGRectGetMinY(aRect)));
            CGContextAddLineToPoint(context, CGRectGetMinX(aRect) + 0.5, ROUND(CGRectGetMaxY(aRect)) - 1.0);
            CGContextClosePath(context);
            CGContextStrokePath(context);
            var ___r1;
        }

        ,["void","CGRect"]), new objj_method(sel_getUid("layoutSubviews"), function $_CPCornerView__layoutSubviews(self, _cmd)
        {
            (self.isa.method_msgSend["setBackgroundColor:"] || _objj_forward)(self, "setBackgroundColor:", (self.isa.method_msgSend["currentValueForThemeAttribute:"] || _objj_forward)(self, "currentValueForThemeAttribute:", "background-color"));
        }

        ,["void"]), new objj_method(sel_getUid("_init"), function $_CPCornerView___init(self, _cmd)
        {
            (self.isa.method_msgSend["setBackgroundColor:"] || _objj_forward)(self, "setBackgroundColor:", (self.isa.method_msgSend["currentValueForThemeAttribute:"] || _objj_forward)(self, "currentValueForThemeAttribute:", "background-color"));
        }

        ,["void"]), new objj_method(sel_getUid("initWithFrame:"), function $_CPCornerView__initWithFrame_(self, _cmd, aFrame)
        {
            self = (objj_getClass("_CPCornerView").super_class.method_dtable["initWithFrame:"] || _objj_forward)(self, "initWithFrame:", aFrame);
            if (self)
                (self == null ? null : (self.isa.method_msgSend["_init"] || _objj_forward)(self, "_init"));
            return self;
        }

        ,["id","CGRect"]), new objj_method(sel_getUid("initWithCoder:"), function $_CPCornerView__initWithCoder_(self, _cmd, aCoder)
        {
            self = (objj_getClass("_CPCornerView").super_class.method_dtable["initWithCoder:"] || _objj_forward)(self, "initWithCoder:", aCoder);
            if (self)
                (self == null ? null : (self.isa.method_msgSend["_init"] || _objj_forward)(self, "_init"));
            return self;
        }

        ,["id","CPCoder"])]);
    class_addMethods(meta_class, [new objj_method(sel_getUid("defaultThemeClass"), function $_CPCornerView__defaultThemeClass(self, _cmd)
        {
            return "cornerview";
        }

        ,["CPString"]), new objj_method(sel_getUid("themeAttributes"), function $_CPCornerView__themeAttributes(self, _cmd)
        {
            return (___r1 = (CPDictionary.isa.method_msgSend["alloc"] || _objj_forward)(CPDictionary, "alloc"), ___r1 == null ? null : (___r1.isa.method_msgSend["initWithObjects:forKeys:"] || _objj_forward)(___r1, "initWithObjects:forKeys:", [(CPNull.isa.method_msgSend["null"] || _objj_forward)(CPNull, "null"), (CPNull.isa.method_msgSend["null"] || _objj_forward)(CPNull, "null")], ["background-color", "divider-color"]));
            var ___r1;
        }

        ,["CPDictionary"])]);
}
e;