/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.wave;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.totsp.crossword.puz.Puzzle;

import com.totsp.crossword.web.client.BoxView;
import com.totsp.crossword.web.client.Game;
import com.totsp.crossword.web.client.PuzzleDescriptorView;
import com.totsp.crossword.web.client.PuzzleListView;
import com.totsp.crossword.web.client.PuzzleServiceProxy;
import com.totsp.crossword.web.client.PuzzleServiceProxy.CallStrategy;
import com.totsp.crossword.web.client.Renderer;
import com.totsp.crossword.web.client.RetryLocalStorageServiceProxy;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleService;
import com.totsp.crossword.web.shared.PuzzleServiceAsync;
import com.totsp.crossword.web.wave.ShortyzWave.FakeRequest;


/**
 *
 * @author kebernet
 */
public class Module extends AbstractGinModule {
    @Override
    protected void configure() {
        this.bind(Resources.class).toProvider(ResourcesProvider.class);
        this.bind(Renderer.class).asEagerSingleton();
        this.bind(PuzzleServiceAsync.class)
            .toProvider(PuzzleServiceProvider.class);
        this.bind(BoxView.class);
        this.bind(PuzzleDescriptorView.class);
        this.bind(PuzzleListView.class);
        this.bind(Game.class).asEagerSingleton();
        this.bind(PuzzleServiceProxy.class)
            .toProvider(PuzzleServiceProxyProvider.class).asEagerSingleton();
        this.bind(RootPanel.class).toProvider(RootPanelProvider.class);
    }

    public static class RootPanelProvider implements Provider<RootPanel> {

        @Override
        public RootPanel get() {
            return RootPanel.get();
        }

    }

    public static class PuzzleServiceProvider implements Provider<PuzzleServiceAsync> {
        public static PuzzleServiceAsync INSTANCE = null;

        @Override
        public PuzzleServiceAsync get() {
            return (INSTANCE == null)
            ? (INSTANCE = GWT.create(PuzzleService.class)) : INSTANCE;
        }
    }

    public static class PuzzleServiceProxyProvider implements Provider<PuzzleServiceProxy> {
        PuzzleServiceAsync service;

        @Inject
        PuzzleServiceProxyProvider(PuzzleServiceAsync service) {
            this.service = service;
        }

        @Override
        public PuzzleServiceProxy get() {
            return new RetryLocalStorageServiceProxy(service,  new CallStrategy(){

            @Override
            public Request makeRequest(RequestBuilder builder) {
                 ShortyzWave.makePostRequest(builder.getUrl(), builder.getRequestData(), builder.getCallback());
                 return new FakeRequest();
            }

        }){

                @Override
                public Request savePuzzle(Long listingId, Puzzle puzzle, final AsyncCallback callback) {
                    DeferredCommand.add(new Command(){

                        @Override
                        public void execute() {
                            callback.onSuccess(null);
                        }
                        
                    });
                    return new FakeRequest();
                }

        };
        }
    }

    public static class ResourcesProvider implements Provider<Resources> {
        Resources instance = null;

        @Override
        public Resources get() {
            return (instance == null) ? (instance = GWT.create(Resources.class))
                                      : instance;
        }
    }
}
