package benke.ladislau.attila;

import javax.imageio.plugins.tiff.TIFFTagSet;
import javax.print.DocFlavor;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class frm_Main extends JFrame {
    //region GUI Elements
    private JPanel pnl_Main;
    private JPanel pnl_Sequences;
    private JButton btn_GenSeq;
    private JTextField txt_seq1;
    private JTextField txt_seq2;
    private JTextField txt_seq1_from;
    private JTextField txt_seq2_from;
    private JTextArea txt_m1;
    private JTextArea txt_m2;
    private JButton btn_m1;
    private JButton btn_m2;
    private JTextField txt_seq1_to;
    private JTextField txt_seq2_to;
    //endregion

    //region Constructors
    frm_Main(){
        this("Proiect_LCS");
    }

    frm_Main(String title){
        super.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(pnl_Main);
        this.pack();

        btn_GenSeq.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generate_sequences();
            }
        });

        btn_m1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run_ExhaustiveMethod();
            }
        });

        btn_m2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run_OptimisedMethod();
            }
        });
    }
    //endregion Constructors

    private void generate_sequences(){
        //String for valid characters to be used when generating sequences
        String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        //Random generator
        Random random = new Random();

        //Initialization of sequences
        String seq1 = "";
        String seq2 = "";

        //Choose random length for sequences (based on selected limits within GUI)
        int seq1_length = random.nextInt(Integer.parseInt(txt_seq1_to.getText()) -
                                         Integer.parseInt(txt_seq1_from.getText())) +
                                         Integer.parseInt(txt_seq1_from.getText());
        int seq2_length = random.nextInt(Integer.parseInt(txt_seq2_to.getText()) -
                                         Integer.parseInt(txt_seq2_from.getText())) +
                                         Integer.parseInt(txt_seq2_from.getText());

        //Fill sequence1 with random characters (from validChars) up to chosen length
        for (int cnt = 1; cnt <= seq1_length; cnt++){
            seq1 += validChars.charAt(random.nextInt(validChars.length() - 1));
        }

        //Choose number of characters to remove from sequence 1
        //which will not be transferred to sequence 2
        int cnt_Remove = random.nextInt((int)Math.floor(seq1_length / 2));
        //If amount characters that remained is more than chosen length for sequence 2,
        //then additional characters will be removed
        if ((seq1.length() - cnt_Remove) > seq2_length) {
            cnt_Remove = seq1_length - seq2_length;
        }

        //Create byte array from sequence 1
        byte[] seq2_bytes = seq1.getBytes(StandardCharsets.UTF_8);
        int tmpPos;
        //Replace chosen amount of bytes on random positions with 0 byte
        for (int cnt = 1; cnt <= cnt_Remove; cnt++){
            tmpPos = random.nextInt(seq2_bytes.length - 1) + 1;
            while (seq2_bytes[tmpPos] == 0) {
                tmpPos = random.nextInt(seq2_bytes.length - 1) + 1;
            }
            seq2_bytes[tmpPos] = 0;
        }
        //Transform byte array to temporary sequence and replace 0 bytes with empty string
        String seq2_cut = new String(seq2_bytes).replace(Character.toString(0),"");

        //If temporary sequence's length is less than chosen length,
        //then additional random characters will be added to random positions
        if (seq2_cut.length() < seq2_length) {
            //Create array of random positions
            int[] seq2_addPos = new int[seq2_length - seq2_cut.length()];
            for (int cnt = 0; cnt < seq2_addPos.length; cnt++) {
                seq2_addPos[cnt] = random.nextInt(seq2_cut.length());
            }
            Arrays.sort(seq2_addPos);

            //Add random characters to all positions from array of random positions
            int addPos = 0;
            for (int cnt = 0; cnt <= seq2_cut.length(); cnt++) {
                if (cnt > 0) {
                    seq2 += seq2_cut.charAt(cnt - 1);
                }
                if (addPos < seq2_addPos.length) {
                    while (seq2_addPos[addPos] == cnt) {
                        seq2 += validChars.charAt(random.nextInt(validChars.length() - 1));
                        addPos += 1;
                        if (addPos == seq2_addPos.length) {
                            break;
                        }
                    }
                }
            }
        } else {
            seq2 = seq2_cut;
        }

        txt_seq1.setText(seq1);
        txt_seq2.setText(seq2);
    }

    private void run_ExhaustiveMethod(){
        //Counters for operations
        int[] cntFindSubSeq1 = {1, 1};
        int[] cntFindSubSeq2 = {1, 1};
        int[] cntCompare = {0};
        int[] cntCompOptim = {0};

        //Length of Longest Common Subsequence
        int[] lenLCS = {0};
        int[] lenLCSOptim = {0};

        //Subsequences of Sequence 1
        StringVector subSeq1 = new StringVector();
        StringVector subSeq1_Optim = new StringVector();

        //Subsequences of Sequence 2
        StringVector subSeq2 = new StringVector();
        StringVector subSeq2_Optim = new StringVector();

        //Vector of Longest Common Subsequences
        Vector<String> strLCS;
        Vector<String> strLCSOptim;

        //Find all possible subsequences of Sequence 1
        findSubsequences("",txt_seq1.getText(), subSeq1, subSeq1_Optim, cntFindSubSeq1);
        //Find all possible subsequences of Sequence 2
        findSubsequences("",txt_seq2.getText(), subSeq2, subSeq2_Optim, cntFindSubSeq2);
        //Find all longest common subsequences
        strLCS = getLCS(subSeq1, subSeq2, lenLCS, cntCompare);
        strLCSOptim = getLCS_Optim(subSeq1_Optim, subSeq2_Optim, lenLCSOptim, cntCompOptim);

        //region Display results
        txt_m1.setText("");

        txt_m1.append(" Input:\n\n");

        txt_m1.append("  Sequence 1: " + txt_seq1.getText() + "\n");
        txt_m1.append("  Sequence 2: " + txt_seq2.getText() + "\n\n");

        txt_m1.append("  Sequence 1 length (m) = " + txt_seq1.getText().length() + "\n");
        txt_m1.append("  Sequence 2 length (n) = " + txt_seq2.getText().length() + "\n");

        txt_m1.append("------------------------------------------------------------------------------------------\n");

        txt_m1.append("\n Operations:\n");

        txt_m1.append("\n     Find subsequences of Sequence1 (f1): 2^m");
        txt_m1.append("\n         Max. (f1_max): " + (int)(Math.pow(2, txt_seq1.getText().length())));
        txt_m1.append("\n         Cnt. (f1_cnt): " + cntFindSubSeq1[0]);
        txt_m1.append("\n             All Subsequences of Sequence1: ");
        txt_m1.append("\n               " + subSeq1.getVector().toString());
        txt_m1.append("\n         Opt. (f1_opt): " + cntFindSubSeq1[1]);
        txt_m1.append("\n             Unique Subsequences of Sequence1: ");
        txt_m1.append("\n               " + subSeq1_Optim.getVector().toString() + "\n");

        txt_m1.append("\n     Find subsequences of Sequence2 (f2): 2^n");
        txt_m1.append("\n         Max. (f2_max): " + (int)(Math.pow(2, txt_seq2.getText().length())));
        txt_m1.append("\n         Cnt. (f2_cnt): " + cntFindSubSeq2[0]);
        txt_m1.append("\n             All Subsequences of Sequence2: ");
        txt_m1.append("\n               " + subSeq2.getVector().toString());
        txt_m1.append("\n         Opt. (f2_opt): " + cntFindSubSeq2[1]);
        txt_m1.append("\n             Unique Subsequences of Sequence1: ");
        txt_m1.append("\n               " + subSeq2_Optim.getVector().toString() + "\n");

        txt_m1.append("\n     Compare subsequences of Sequence1 and Sequence2 (c): 2^m * 2^n");
        txt_m1.append("\n         Max. (c_max): " + (((int)(Math.pow(2, txt_seq1.getText().length()))) *
                                                      (int)(Math.pow(2, txt_seq2.getText().length()))));
        txt_m1.append("\n         Cnt. (c_cnt): " + cntCompare[0]);
        txt_m1.append("\n             LCS Length: " + lenLCS[0]);
        txt_m1.append("\n             LCS Item(s): " + strLCS.toString());
        txt_m1.append("\n         Opt. (c_opt): " + cntCompOptim[0]);
        txt_m1.append("\n             LCS Length: " + lenLCSOptim[0]);
        txt_m1.append("\n             LCS Item(s): " + strLCSOptim.toString() + "\n");

        txt_m1.append("------------------------------------------------------------------------------------------\n");

        txt_m1.append("\n Complexity:\n");

        txt_m1.append("\n     Total Operations (o): 2^m + 2^n + 2^m * 2^n");
        txt_m1.append("\n         Max. (o_max): " + ((int)(Math.pow(2, txt_seq1.getText().length())) +
                                                    (int)(Math.pow(2, txt_seq2.getText().length())) +
                                                    ((int)(Math.pow(2, txt_seq1.getText().length())) *
                                                     (int)(Math.pow(2, txt_seq2.getText().length())))));
        txt_m1.append("\n         Cnt. (o_cnt): " + (cntFindSubSeq1[0] + cntFindSubSeq2[0] +
                                                    (cntFindSubSeq1[0] * cntFindSubSeq2[0])));
        txt_m1.append("\n         Opt. (o_opt): " + (cntFindSubSeq1[1] + cntFindSubSeq2[1] +
                                                    (cntFindSubSeq1[1] * cntFindSubSeq2[1])) + "\n");

        txt_m1.append("------------------------------------------------------------------------------------------\n");

        txt_m1.append("\n Results:\n\n");

        txt_m1.append("  Length of LCS: " + lenLCS[0] + "\n");
        txt_m1.append("  Length of LCS (Optimized): " + lenLCSOptim[0] + "\n\n");
        txt_m1.append("  Longest Common Subsequence: " + strLCS.toString() + "\n");
        txt_m1.append("  Longest Common Subsequence (Optimized): " + strLCSOptim.toString() + "\n");
        //endregion
    }

    private void findSubsequences(String crntSubseq,
                                  String sequence,
                                  StringVector subsequences,
                                  StringVector subsequencesOptim,
                                  int[] cntOp) {
        for (int cnt = 1; cnt <= sequence.length(); cnt++) {
            //Add current subsequence + current character to vector of subsequences (even if it's duplicate)
            subsequences.insertByLength(crntSubseq + sequence.charAt(cnt - 1));
            //Increment counter of operations
            cntOp[0] += 1;
            cntOp[1] += 1;
            //If current subsequence + current character is not duplicate, add to optimized vector of subsequences
            if (!subsequencesOptim.getVector().contains(crntSubseq + sequence.charAt(cnt - 1))) {
                subsequencesOptim.insertByLength(crntSubseq + sequence.charAt(cnt - 1));
                //Increment counter of optimized operations
            }
            //Recursive call of method using current subsequence + current character
            //and part of sequence starting from next character
            if (cnt < sequence.length()) {
                findSubsequences(crntSubseq + sequence.charAt(cnt - 1),
                                 sequence.substring(cnt),
                                 subsequences,
                                 subsequencesOptim,
                                 cntOp);
            }
        }
    }

    private Vector<String> getLCS(StringVector subsequences1, StringVector subsequences2, int[] lenLCS, int[] cntOp) {
        //Local vector for all longest subsequences
        Vector<String> loc_LCS = new Vector<String>();

        //Iterate through all subsequnces of sequence 1
        for(int cnt1 = 0; cnt1 < subsequences1.getVector().size(); cnt1++) {
            //Iterate through all subsequnces of sequence 2
            for (int cnt2 = 0; cnt2 < subsequences2.getVector().size(); cnt2++) {
                //Increment counter of operations
                cntOp[0] += 1;
                //If current subsequence of sequence 1 is equal to current subsequence of sequence 2,
                //is not shorter than already found common subsequence(s) and is not duplicate,
                //then add it to vector for all longest common subsequences.
                if (subsequences1.getVector().elementAt(cnt1).equals(subsequences2.getVector().elementAt(cnt2)) &&
                        subsequences1.getVector().elementAt(cnt1).length() >= lenLCS[0] &&
                        !loc_LCS.contains(subsequences1.getVector().elementAt(cnt1))) {
                    //If current common subsequence is longer than already found subseqence(s),
                    //then reset vector for all longest common subsequences.
                    if (subsequences1.getVector().elementAt(cnt1).length() > lenLCS[0]) {
                        loc_LCS = new Vector<String>();
                        //If found common subsequence is not empty subsequence,
                        //then set length of common subsequence(s) to length of found common subsequence
                        if (!subsequences1.getVector().elementAt(cnt1).equals("0")) {
                            lenLCS[0] = subsequences1.getVector().elementAt(cnt1).length();
                        }
                    }
                    //Add found current common subsequence to vector of found common subsequence(s)
                    loc_LCS.add(subsequences1.getVector().elementAt(cnt1));
                }
            }
        }

        //Return all found longest common subsequences
        return loc_LCS;
    }

    private Vector<String> getLCS_Optim(StringVector subsequences1, StringVector subsequences2, int[] lenLCS, int[] cntOp) {
        //Local vector for all longest common subsequences
        Vector<String> loc_LCS = new Vector<String>();

        //Iterate through all subsequences of sequence 1, starting with the longest subsequences
        for(int cnt1 = subsequences1.getVector().size(); cnt1 > 1; cnt1--) {
            //If length of current subsequence of sequence 1 is shorter then length of found common subsequence(s),
            //then stop searching additional common subsequences
            if (subsequences1.getVector().elementAt(cnt1 - 1).length() < lenLCS[0]) {
                break;
            } else {
                //Iterate through all subsequences of sequence 2, starting with the longest subsequences
                for (int cnt2 = subsequences2.getVector().size(); cnt2 > 1; cnt2--) {
                    //If length of current subsequence of sequence 2 is shorter
                    //than length of found common subsequence(s),
                    //then stop searching additional common subsequences
                    if (subsequences2.getVector().elementAt(cnt2 - 1).length() < lenLCS[0]) {
                        break;
                    } else {
                        //Increment counter of operations
                        cntOp[0] += 1;
                        //If current subsequence of sequence 1 is equal to current subsequence of sequence2
                        //and is not duplicate, then add it to vector for all longest common subsequences
                        if (subsequences1.getVector().elementAt(cnt1 - 1).equals(
                                subsequences2.getVector().elementAt(cnt2 - 1)) &&
                                !loc_LCS.contains(subsequences1.getVector().elementAt(cnt1 - 1))) {
                            //If length of found subsequence is longer than length of already found subsequence(s),
                            //then reset vector for all longest common subsequences
                            if (subsequences1.getVector().elementAt(cnt1 - 1).length() > lenLCS[0]) {
                                loc_LCS = new Vector<String>();
                                //If found common subsequence is not empty subsequence,
                                //then set length of common subsequence(s) to length of found common subsequence
                                if (!subsequences1.getVector().elementAt(cnt1 - 1).equals("0")) {
                                    lenLCS[0] = subsequences1.getVector().elementAt(cnt1 - 1).length();
                                }
                            }
                            //Add found current common subsequence to vector of found common subsequence(s)
                            loc_LCS.add(subsequences1.getVector().elementAt(cnt1 - 1));
                        }
                    }
                }
            }
        }

        //If no common subsequence found,
        //then add empty subsequence to vector of longest common subsequences
        if (loc_LCS.size() == 0) {
            loc_LCS.add("0");
        }
        //Return all found longest common subsequences
        return loc_LCS;
    }

    private void run_OptimisedMethod() {
        int[] cntMatrix = {0};
        int[] lenLCS = {0};
        Vector<String> strLCS;
        MatrixElement[][] lcsMatrix = new MatrixElement[1][1];

        lcsMatrix = getLCS2(txt_seq1.getText(), txt_seq2.getText(), lenLCS, cntMatrix);

        //region Display results
        txt_m2.setText("");

        txt_m2.append(" Input:\n\n");

        txt_m2.append("  Sequence 1: " + txt_seq1.getText() + "\n");
        txt_m2.append("  Sequence 2: " + txt_seq2.getText() + "\n\n");

        txt_m2.append("  Sequence 1 length (m) = " + txt_seq1.getText().length() + "\n");
        txt_m2.append("  Sequence 2 length (n) = " + txt_seq2.getText().length() + "\n");

        txt_m2.append("------------------------------------------------------------------------------------------\n");


        txt_m2.append("\n Operations:\n");

        txt_m2.append("\n     Create 2-dimensional matrix (t): m * n");
        txt_m2.append("\n         Max. steps (t_max): " + (txt_seq1.getText().length() * txt_seq2.getText().length()));
        txt_m2.append("\n         Cnt. steps (t_cnt): " + cntMatrix[0]);

        String loc_Row1 = "";
        String loc_Row2 = "";
        String loc_Row3 = "";

        try {
            for (int cnt1 = -1; cnt1 < lcsMatrix.length - 2; cnt1++) {
                loc_Row1 = "";
                loc_Row2 = "";
                loc_Row3 = "";
                if (cnt1 == -1) {
                    loc_Row2 = "                 ";
                    loc_Row3 = "                 ";
                    for (int cnt2 = 1; cnt2 < lcsMatrix[0].length; cnt2++) {
                        loc_Row2 += "┌─" + "─".repeat(lcsMatrix[lcsMatrix.length - 1][cnt2].getValue() + 1) + "─";
                        loc_Row3 += "│ " + lcsMatrix[0][cnt2].subsequences.toString() + " ".repeat(lcsMatrix[lcsMatrix.length - 1][cnt2].getValue()) + " ";
                    }
                    loc_Row2 += "┐";
                    loc_Row3 += "│";
                } else {
                    if (cnt1 == 0) {
                        loc_Row1 = "             ┌───";
                    } else {
                        loc_Row1 = "             ├───";
                    }
                    loc_Row2 = "             │ " + lcsMatrix[cnt1 + 1][0].subsequences.toString() + " ";
                    loc_Row3 = "             │   ";
                    for (int cnt2 = 1; cnt2 < lcsMatrix[0].length; cnt2++) {
                        loc_Row1 += "┼─" + "─".repeat(lcsMatrix[lcsMatrix.length - 1][cnt2].getValue() + 1) + "─";
                        loc_Row2 += "│ " + lcsMatrix[cnt1 + 1][cnt2].getValue() + " ".repeat(lcsMatrix[lcsMatrix.length - 1][cnt2].getValue() + 1 - String.valueOf(lcsMatrix[cnt1 + 1][cnt2].getValue()).length()) + " ";
                        loc_Row3 += "│ " + lcsMatrix[cnt1 + 1][cnt2].subsequences.toString() + " ".repeat(lcsMatrix[lcsMatrix.length - 1][cnt2].getValue() + 1 - lcsMatrix[cnt1 + 1][cnt2].subsequences.toString().length()) + " ";
                    }
                    loc_Row1 += "┤";
                    loc_Row2 += "│";
                    loc_Row3 += "│";
                }
                txt_m2.append("\n" + loc_Row1 + "\n" + loc_Row2 + "\n" + loc_Row3);
            }
            txt_m2.append("\n             └───");
            for (int cnt2 = 1; cnt2 < lcsMatrix[0].length; cnt2++) {
                txt_m2.append("└─" + "─".repeat(lcsMatrix[lcsMatrix.length - 1][cnt2].getValue() + 1) + "─");
            }
            txt_m2.append("┘\n");
        } catch (Exception ex) {
            txt_m2.append("\n" + loc_Row1 + "\n" + loc_Row2 + "\n" + loc_Row3);
            txt_m2.append("Error: " + ex.getMessage());
        }

        txt_m2.append("------------------------------------------------------------------------------------------\n");

        txt_m2.append("\n Complexity:\n");

        txt_m2.append("\n     Total Operations (o): m * n");
        txt_m2.append("\n         Max. (o_max): " + (txt_seq1.getText().length() * txt_seq2.getText().length()));
        txt_m2.append("\n         Cnt. (o_cnt): " + cntMatrix[0] + "\n");

        txt_m2.append("------------------------------------------------------------------------------------------\n");

        txt_m2.append("\n Results:\n\n");

        txt_m2.append("  Length of LCS: " + lcsMatrix[txt_seq1.getText().length()][txt_seq2.getText().length()].getValue() + "\n");
        txt_m2.append("  Longest Common Subsequence(s): " + lcsMatrix[txt_seq1.getText().length()][txt_seq2.getText().length()].getSubsequences().getVector().toString() + "\n");

        //endregion
    }

    private MatrixElement[][] getLCS2(String sequence1, String sequence2, int[] lenLCS, int[] cntOP) {
        MatrixElement[][] tmpMatrix;

        //Initialize 2D matrix of size (m + 2) * (n + 1):
        // one row reserved for column headers,
        // m rows for each character of sequence 1,
        // one row for column widths (useful for displaying results)
        // one column reserved for row headers,
        // n columns for each character of sequence 2;
        tmpMatrix = new MatrixElement[sequence1.length() + 2][];
        tmpMatrix[0] = new MatrixElement[sequence2.length() + 1];
        tmpMatrix[tmpMatrix.length - 1] = new MatrixElement[sequence2.length() + 1];
        for (int cnt = 0; cnt < sequence2.length() + 1; cnt++) {
            if (cnt > 0) {
                tmpMatrix[0][cnt] = new MatrixElement(0, String.valueOf(sequence2.charAt(cnt - 1)));
            } else {
                tmpMatrix[0][cnt] = new MatrixElement();
            }
            tmpMatrix[tmpMatrix.length - 1][cnt] = new MatrixElement();
        }

        //Iterate through rows 1 to m of 2D matrix
        for (int cnt1 = 1; cnt1 <= sequence1.length(); cnt1++) {
            //Initialize cells of current row
            tmpMatrix[cnt1] = new MatrixElement[sequence2.length() + 1];
            tmpMatrix[cnt1][0] = new MatrixElement(0, String.valueOf(sequence1.charAt(cnt1 - 1)));
            //Iterate through cells of current row
            for (int cnt2 = 1; cnt2 <= sequence2.length(); cnt2++) {
                //Increment counter of operations
                cntOP[0] += 1;
                //If row header and column header of current cell are equal,
                //then current cell's subsequences will be filled with subsequence(s)
                //of the cell from one column to the left and one column to the top.
                //Filled subsequence(s) will get the character from current row header appended.
                //Current cell's value will also be set to the length of new subsequence(s).
                //If current cell belongs to first column and/or first row, the cell to the top-left
                //is one of the row headers or column headers, which are not filled with any subsequence,
                //so none will be filled to current cell, it will be filled only with the character from
                //current row header.
                if (sequence1.charAt(cnt1 - 1) == sequence2.charAt(cnt2 - 1)) {
                    tmpMatrix[cnt1][cnt2] = new MatrixElement(tmpMatrix[cnt1 - 1][cnt2 - 1].getValue() + 1,
                                                           tmpMatrix[cnt1 - 1][cnt2 - 1].getSubsequences());
                    tmpMatrix[cnt1][cnt2].appendToSubsequences(sequence1.charAt(cnt1 - 1));
                } else {
                    //If row header and column header of current cell are not equal,
                    //then the length of subsequence(s) from the cell to the left of
                    //current cell are compared with the length of subsequence(s) from
                    //the cell to the top of current cell.
                    //If length of subsequence(s) of the cell to the left are bigger,
                    //then subsequence(s) of it are copied to the current cell.
                    if (tmpMatrix[cnt1 - 1][cnt2].getValue() > tmpMatrix[cnt1][cnt2 - 1].value) {
                        tmpMatrix[cnt1][cnt2] = new MatrixElement(tmpMatrix[cnt1 - 1][cnt2].value,
                                                                  tmpMatrix[cnt1 - 1][cnt2].getSubsequences());
                    //If length of subsequence(s) of the cell to the top are bigger,
                    //then subsequence(s) of it are copied to the current cell.
                    } else if (tmpMatrix[cnt1 - 1][cnt2].getValue() < tmpMatrix[cnt1][cnt2 - 1].value) {
                        tmpMatrix[cnt1][cnt2] = new MatrixElement(tmpMatrix[cnt1][cnt2 - 1].value,
                                                                  tmpMatrix[cnt1][cnt2 - 1].getSubsequences());
                    //If length of subsequence(s) of the cell to the left is/are equal to the length of subsequence(s)
                    //of the cell to the top, then subsequence(s) of both are copied to the current cell.
                    } else {
                        tmpMatrix[cnt1][cnt2] = new MatrixElement(tmpMatrix[cnt1 - 1][cnt2].value,
                                                                  tmpMatrix[cnt1 - 1][cnt2].getSubsequences());
                        tmpMatrix[cnt1][cnt2].addSubsequences(tmpMatrix[cnt1][cnt2 - 1].getSubsequences());
                    }
                }
                //Set value of cell from last row and current column according to maximum required number of characters
                //(it is the maximum length of string representations of subsequence(s0 in current column and is used
                //when displaying results.)
                tmpMatrix[tmpMatrix.length - 1][cnt2].setValue(Math.max(tmpMatrix[cnt1][cnt2].subsequences.toString().length(),
                        tmpMatrix[tmpMatrix.length - 1][cnt2].getValue()));
            }
        }

        //Return filled 2D matrix.
        //The cell at the bottom-right will contain all longest common subsequence(s).
        //(last row of 2D matrix - which is used when displaying results - excluded)
        return tmpMatrix;
    }
}

//Class to store subsequences sorted by length
class StringVector {
    Vector<String> l_Strings;

    StringVector(){
        l_Strings = new Vector<String>();
        l_Strings.add("0");
    }

    StringVector(boolean withNullString) {
        l_Strings = new Vector<String>();
        if (withNullString) {
            l_Strings.add("0");
        }
    }

    StringVector(String string){
        l_Strings = new Vector<String>();
        l_Strings.add(string);
    }

    public void insertByLength(String string){
        //Find position where to insert string, so vector keep being sorted by length of all strings
        for (int cnt = 0; cnt <= l_Strings.size(); cnt++) {
            if (cnt == l_Strings.size()) {
                l_Strings.add(string);
                break;
            } else if (l_Strings.elementAt(cnt).length() > string.length()) {
                l_Strings.insertElementAt(string,cnt);
                break;
            }
        }
    }

    public String toString() {
        return this.l_Strings.toString().replace("[", "").replace("]","");
    }

    public Vector<String> getVector() {
        return l_Strings;
    }
}

class MatrixElement {
    enum Path {None, Left, Up, Diagonal}

    int value;
    StringVector subsequences;

    MatrixElement() {
        this(0);
    }

    MatrixElement(int value) {
        this(value, "");
    }

    MatrixElement(int value,  String subsequence) {
        this(value, new StringVector(subsequence));
    }

    MatrixElement(int value, StringVector subsequences) {
        this.subsequences = new StringVector(false);

        this.value = value;
        for (int cnt = 0; cnt < subsequences.getVector().size(); cnt++) {
            this.subsequences.insertByLength(subsequences.getVector().elementAt(cnt));
        }
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public StringVector getSubsequences() {
        if (this.value != 0) {
            return this.subsequences;
        } else {
            return new StringVector(false);
        }
    }

    public void addSubsequence(String subsequence) {
        this.subsequences.insertByLength(subsequence);
    }

    public void addSubsequences(StringVector subsequences){
        for (int cnt = 0; cnt < subsequences.getVector().size(); cnt++) {
            if (!this.subsequences.getVector().contains(subsequences.getVector().elementAt(cnt))) {
                this.subsequences.insertByLength(subsequences.getVector().elementAt(cnt));
            }
        }
    }

    public void appendToSubsequences(char character) {
        if (this.subsequences.getVector().size() == 0) {
            this.subsequences.insertByLength(String.valueOf(character));
        } else {
            for (int cnt = 0; cnt < this.subsequences.getVector().size(); cnt++) {
                this.subsequences.getVector().setElementAt(this.subsequences.getVector().elementAt(cnt).concat(String.valueOf(character)),cnt);
            }
        }
    }
}
