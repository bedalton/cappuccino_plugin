package org.cappuccino_project.ide.intellij.plugin.contributor.utils;

import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncEditAction extends AnAction {
    private static final Logger LOGGER = Logger.getInstance(SyncEditAction.class.getName());
    private static final Integer ONE = 1;

    public SyncEditAction() {
        super("SyncEdit");
    }

    public void update(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        boolean enabled = false;
        Editor editor = dataContext.getData(DataKeys.EDITOR);
        if (editor != null) {
            SelectionModel selectionModel = editor.getSelectionModel();
            enabled = selectionModel.hasSelection();
        }
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
    }

    public void actionPerformed(AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        Project project = dataContext.getData(DataKeys.PROJECT);
        if (project == null) {
            LOGGER.error("Cannot perform sync edit action. Project is null");
            return;
        }

        TemplateManager templateManager = TemplateManager.getInstance(project);
        Editor editor = dataContext.getData(DataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection()) {
            Document document = editor.getDocument();
            boolean isWritable = document.isWritable();
            if (!isWritable) {
                return;
            }
            int selectionStart = selectionModel.getSelectionStart();
            int selectionEnd = selectionModel.getSelectionEnd();
            CharSequence content = document.getCharsSequence();
            String selection = content.subSequence(selectionStart, selectionEnd).toString();
            List<String> words = splitIntoWords(selection);
            Map<String, Integer> countMap = countWords(words);
            final List<String> variables = getVariables(words, countMap);
            final String text = joinWords(words);

            TemplateImpl tempTemplate = createTemplate(text, variables);

            templateManager.startTemplate(editor, "", tempTemplate);
        }
    }



    private static List<String> getVariables(List<String> words, Map<String, Integer> wordCountMap) {
        final ArrayList<String> variables = new ArrayList<>();
        final int wordCount = words.size();
        final Map<String, Integer> word2VariableIndex = new HashMap<>();
        for (int wordIndex = 0; wordIndex < wordCount; wordIndex++) {
            String word = words.get(wordIndex);
            if (isWordChar(word.charAt(0))) {
                int count = wordCountMap.get(word);
                if (count > 1) {
                    Integer variableIndexInteger = word2VariableIndex.get(word);
                    int variableIndex;
                    if (variableIndexInteger != null) {
                        variableIndex = variableIndexInteger;
                    }
                    else {
                        variableIndex = variables.size();
                        word2VariableIndex.put(word, variableIndex);
                        variables.add(word);
                    }
                    final String variableName = getVariableName(variableIndex);
                    String variableExpression = '$' + variableName + '$';
                    words.set(wordIndex, variableExpression);
                }
            }
        }
        return variables;
    }

    private static Map<String, Integer> countWords(List words) {
        final Map<String, Integer> map = new HashMap<>();
        for (Object word1 : words) {
            String word = (String) word1;
            if (isWordChar(word.charAt(0))) {
                Integer countInteger = map.get(word);
                if (countInteger == null) {
                    map.put(word, ONE);
                } else {
                    int count = countInteger;
                    map.put(word, count + 1);
                }
            }
        }
        return map;
    }


    private static List<String> splitIntoWords(final String text) {
        int length = text.length();
        ArrayList<String> words = new ArrayList<>(length / 3);
        int wordStartIndex = 0;
        boolean isWordChar = isWordChar(text.charAt(0));
        int i;
        for (i = 1; i < length; ++i) {
            boolean newIsWordChar = isWordChar(text.charAt(i));
            if (isWordChar != newIsWordChar) {
                String word = text.substring(wordStartIndex, i);
                words.add(word);
                wordStartIndex = i;
            }
            isWordChar = newIsWordChar;
        }
        if (wordStartIndex != length) {
            String word = text.substring(wordStartIndex, i);
            words.add(word);
        }
        return words;
    }

    public static void main(String[] args) {
        final List words = splitIntoWords(" Ha 34lk bubuu ");
        for (Object o : words) {
            System.out.println(">" + o + "<-");
        }
    }

    private static boolean isWordChar(char c) {
        return Character.isLetter(c) || Character.isDigit(c);
    }

    private static String joinWords(List words) {
        final StringBuffer buffer = new StringBuffer();
        for (Object word1 : words) {
            String word = (String) word1;
            buffer.append(word);
        }
        return new String(buffer);
    }

    private static String getVariableName(int i) {
        return 'v' + Integer.toString(i);
    }

    private TemplateImpl createTemplate(String text, List variables) {
        TemplateImpl tempTemplate = new TemplateImpl("", text, "other");

        tempTemplate.setKey("hi");
        tempTemplate.setDescription("sync edit template");
        tempTemplate.setToIndent(false);
        tempTemplate.setToReformat(false);
        tempTemplate.setToShortenLongNames(false);
        tempTemplate.parseSegments();
        tempTemplate.setInline(false);

        final int variableCount = variables.size();
        for (int i = 0; i < variableCount; i++) {
            String value = (String) variables.get(i);
            String name = getVariableName(i);
            String quotedValue = '"' + value + '"';
            tempTemplate.addVariable(name, quotedValue, quotedValue, true);
        }
        return tempTemplate;
    }
}