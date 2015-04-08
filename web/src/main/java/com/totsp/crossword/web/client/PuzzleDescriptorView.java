/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;

import com.google.inject.Inject;

import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.crossword.web.shared.PuzzleDescriptor;

import com.totsp.gwittir.client.ui.AbstractBoundWidget;
import com.totsp.gwittir.client.ui.util.BoundWidgetProvider;


/**
 *
 * @author kebernet
 */
public class PuzzleDescriptorView extends AbstractBoundWidget<PuzzleDescriptor> {
    

    DateTimeFormat format = DateTimeFormat.getFormat(
            "EEEE '<br \\>' MMM dd, yyyy");
    HTML date = new HTML();
    Label source = new Label();
    Label title = new Label();
    private PuzzleDescriptor value;

    @Inject
    public PuzzleDescriptorView(final Resources resources, final Game game) {
        FocusPanel fp = new FocusPanel();
        fp.addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    addStyleName(resources.css().pdOver());
                }
            });
        fp.addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    removeStyleName(resources.css().pdOver());
                }
            });

        FlexTable table = new FlexTable();
        table.insertRow(0);
        table.setWidget(0, 0, date);
        table.getCellFormatter().setWidth(0, 0, "25%");
        table.setWidget(0, 1, source);
        table.getCellFormatter().setWidth(0, 1, "50%");
        table.getCellFormatter()
             .setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
        table.setWidget(0, 2, title);
        table.getCellFormatter().setWidth(0, 2, "25%");

        date.setStyleName(resources.css().pdDate());
        title.setStyleName(resources.css().pdTitle());
        source.setStyleName(resources.css().pdSource());
        table.setWidth("100%");
        fp.setWidget(table);
        super.initWidget(fp);
        this.setStyleName(resources.css().pd());

        table.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    game.loadPuzzle(getValue().getId());
                }
            });
    }

    @Override
    public void setModel(Object model) {
        this.setValue((PuzzleDescriptor) model);
        super.setModel(model);
    }

    /**
     * Set the value of value
     *
     * @param newvalue new value of value
     */
    @Override
    public void setValue(PuzzleDescriptor newvalue) {
        this.value = newvalue;

        if (value == null) {
            return;
        }

        this.date.setHTML((value.getDate() != null)
            ? format.format(value.getDate()) : "");
        this.source.setText(value.getSource());
        this.title.setText(value.getTitle());
    }

    /**
     * Get the value of value
     *
     * @return the value of value
     */
    @Override
    public PuzzleDescriptor getValue() {
        return this.value;
    }
}
