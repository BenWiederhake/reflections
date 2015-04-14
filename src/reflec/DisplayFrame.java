package reflec;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public final class DisplayFrame extends JFrame {
    /** Not meant for serialization. */
    private static final long serialVersionUID = 1L;
    
    private static final String FILE_NAME = "buildings.txt";

    private final DefaultMutableModel mutModel;

    private final DefaultPainter.Factory factory;

    public DisplayFrame() {
        final Model model;
        try {
            model = Model.from(new Scanner(new File(FILE_NAME)));
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        mutModel = new DefaultMutableModel(model);
        factory = new DefaultPainter.Factory();
        final DisplayPanel panel = new DisplayPanel(
            factory, PhonyBuffer.FACTORY, mutModel);
        mutModel.add(panel);
//        CursorMarker.hookInto(panel);
        MouseInteractivity.hookInto(mutModel, panel);
        RecursionPrinter.hookInto(panel, mutModel);
        DepthScroller.hookInto(mutModel, panel);

        final JCheckBox chkBox = new JCheckBox("Draw Beams");
        chkBox.setSelected(factory.isPaintingBeams());
        chkBox.setFocusPainted(false);
        chkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (chkBox.isSelected() != factory.isPaintingBeams()) {
                    factory.setPaintingBeams(chkBox.isSelected());
                    panel.notifyModelChanged(false);
                }
            }
        });
        
        final JPanel content = new JPanel(new BorderLayout(0, 0));
        content.add(panel);
        content.add(chkBox, BorderLayout.SOUTH);
        
        setTitle("Reflections v0.5");
        setContentPane(content);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // pack(); // ?
        setSize(640, 480);
    }

    public DefaultMutableModel getMutModel() {
        return mutModel;
    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                final DisplayFrame win = new DisplayFrame();
                win.setVisible(true);
            }
        });
    }
}
