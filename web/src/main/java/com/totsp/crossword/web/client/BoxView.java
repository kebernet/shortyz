/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.web.client.resources.Resources;
import com.totsp.gwittir.client.ui.AbstractBoundWidget;

/**
 *
 * @author kebernet
 */
public class BoxView extends AbstractBoundWidget<Box> {

    public static String PROP_VALUE = "value";

    private Label number = new Label();
    private Label letter = new Label();
    private Box value;

    @Inject
    public BoxView(Resources resources){
        AbsolutePanel main = new AbsolutePanel();
        
        number.setStyleName(resources.css().number());
        letter.setStyleName(resources.css().letter());
        main.add(number, 0,0);
        main.add(letter, 0,0);

        super.initWidget(main);
        this.setStyleName(resources.css().boxPanel());
    }

    public Widget getLetter(){
        return this.letter;
    }

    @Override
    public Box getValue() {
        return this.value;
    }

    @Override
    public void setValue(Box value) {
       if(value.isAcross() || value.isDown() ){
           this.number.setText(Integer.toString(value.getClueNumber()));
       }
       letter.setText(Character.toString(value.getResponse()));
       this.changes.firePropertyChange(PROP_VALUE, this.value, this.value = value);
    }

}
