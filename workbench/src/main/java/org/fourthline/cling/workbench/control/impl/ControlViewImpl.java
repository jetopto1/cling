/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fourthline.cling.workbench.control.impl;

import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.workbench.Workbench;
import org.fourthline.cling.workbench.control.ControlView;
import org.seamless.swing.Application;

import javax.annotation.PostConstruct;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Christian Bauer
 */
public class ControlViewImpl extends JDialog implements ControlView {

    final protected JToolBar invocationToolBar = new JToolBar();

    final protected JButton invokeActionButton =
            new JButton("Invoke", Application.createImageIcon(Workbench.class, "img/16/execute.png"));

    protected JScrollPane inputArgumentsScrollPane;
    protected ActionArgumentTable inputArgumentsTable;

    protected JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    protected JScrollPane outputArgumentsScrollPane;
    protected ActionArgumentTable outputArgumentsTable;

    protected Presenter presenter;

    @Override
    public Component asUIComponent() {
        return this;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @PostConstruct
    public void init() {
        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        dispose();
                    }
                }
        );

        setMinimumSize(new Dimension(300,150));
        setResizable(true);

        invocationToolBar.setMargin(new Insets(5, 0, 5, 0));
        invocationToolBar.setFloatable(false);

        invokeActionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                presenter.onInvoke();
            }
        });
        invokeActionButton.setPreferredSize(new Dimension(5000, 25));
        invocationToolBar.add(invokeActionButton);
    }

    @Override
    public void init(Action action, ActionArgumentValue[] presetInputValues) {

        inputArgumentsTable = new ActionArgumentTable(action, true) {
            @Override
            protected void onExpandText(String text) {
                presenter.onExpandText(text);
            }
        };
        outputArgumentsTable = new ActionArgumentTable(action, false) {
            @Override
            protected void onExpandText(String text) {
                presenter.onExpandText(text);
            }
        };

        inputArgumentsTable.getArgumentValuesModel().setValues(presetInputValues);

        inputArgumentsScrollPane = new JScrollPane(inputArgumentsTable);
        outputArgumentsScrollPane = new JScrollPane(outputArgumentsTable);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(invocationToolBar, BorderLayout.NORTH);

        if (action.hasInputArguments() && action.hasOutputArguments()) {
            splitPane.setTopComponent(inputArgumentsScrollPane);
            splitPane.setBottomComponent(outputArgumentsScrollPane);
            splitPane.setResizeWeight(0.5);
            mainPanel.add(splitPane, BorderLayout.CENTER);
        } else if (action.hasInputArguments()) {
            mainPanel.add(inputArgumentsScrollPane, BorderLayout.CENTER);
        } else if (action.hasOutputArguments()) {
            mainPanel.add(outputArgumentsScrollPane, BorderLayout.CENTER);
        }
        add(mainPanel);

        setTitle("Invoking Action: " + action.getName());
        setPreferredSize(new Dimension(450, (action.getArguments().length *40) + 120));
        pack();
        setVisible(true);
    }

    @Override
    public ActionArgumentValue[] getInputValues() {
        if (inputArgumentsTable == null) return null;
        // Commit the currently typed value
        if (inputArgumentsTable.getCellEditor() != null)
            inputArgumentsTable.getCellEditor().stopCellEditing();

        return inputArgumentsTable.getArgumentValuesModel().getValues();
    }

    @Override
    public void setOutputValues(ActionArgumentValue[] values) {
        if (outputArgumentsTable == null) return;
        outputArgumentsTable.getArgumentValuesModel().setValues(values);
        validate();
    }

}
