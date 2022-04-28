package Interpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CFPL_IDE {
    private JTextArea inputTxtArea;
    private JButton runButton;
    private JPanel paneljPanel;
    public JTextArea outputTxtArea;
    private JTextField OUPUTTextField;


    public CFPL_IDE(){
        setComponents();
    }


    public void setComponents(){
        paneljPanel.setPreferredSize(new Dimension(500,500));
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                outputTxtArea.setText("");
                Main.runProgram(inputTxtArea.getText());
                setOutputTxt(Main.getOutput());
            }
        });
    }

    public static void main(String[] args) {
        JFrame jframe = new JFrame("IDE");
        jframe.setContentPane(Main.ide.paneljPanel);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.pack();
        jframe.setVisible(true);
    }

    public void setOutputTxt(String output){
        outputTxtArea.setText(output);
    }

}
