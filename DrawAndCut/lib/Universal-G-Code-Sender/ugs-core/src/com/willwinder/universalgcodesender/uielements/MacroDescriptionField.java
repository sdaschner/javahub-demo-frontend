package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MacroDescriptionField extends JTextField {

    private final Integer index;
    private final Settings settings;

    public MacroDescriptionField(final Integer index, Settings settings) {
        this.index = index;
        this.settings = settings;
        this.setToolTipText(Localization.getString("macroPanel.description"));

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                update();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                update();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                update();
            }
        });
    }

    private void update() {
        Macro macro = this.settings.getMacro(this.index);
        this.settings.updateMacro(this.index, macro.getName(), this.getText(), macro.getGcode());
    }
}
