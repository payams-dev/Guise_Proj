package dev;

import content.ContentCatalog;
import content.ContentFiles;
import content.RaceDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class DemonConfig extends JFrame {

    private final ContentCatalog sharedContent = new ContentCatalog(ContentFiles::read);
    private final DemonDocument documents = new DemonDocument(sharedContent);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private JTextField nameField;
    private JTextField movesPathField;
    private JTextField searchField;

    private JComboBox<RaceDefinition> raceBox;
    private JSpinner levelSpinner;

    private JLabel basicAttackValueLabel;
    private JLabel statusLabel;
    private JLabel budgetLabel;

    private JTextArea jsonOutputArea;

    private JTable moveTable;
    private DefaultTableModel moveTableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private final DefaultListModel<SkillPick> battleSkillModel = new DefaultListModel<>();
    private final DefaultListModel<SkillPick> learnableSkillModel = new DefaultListModel<>();
    private JList<SkillPick> battleSkillList;
    private JList<SkillPick> learnableSkillList;

    private JTable affinityTable;
    private AffinityTableModel affinityTableModel;

    // --- Capacity UI Components ---
    private JSpinner learnCapacitySpinner;
    private JLabel learnCapacityLabel;
    private JButton addLearnableButton;

    private final MoveRepository moveRepository = new MoveRepository();

    public DemonConfig() {
        super("Oldsona Demon Generator");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 850);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        root.add(buildTopPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        updateRaceDerivedFields();
        refreshMoveTable();
        updateAffinityBudget();
        updateLearnableCapacityUI(); // Initialize capacity state
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Demon Setup"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField("Orpheus", 18);
        raceBox = new JComboBox<>(sharedContent.races.values().toArray(new RaceDefinition[0]));
        levelSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        movesPathField = new JTextField("C:\\Guise_Proj\\assets\\moves\\moves.JSON", 20);
        searchField = new JTextField(18);

        basicAttackValueLabel = new JLabel("-");
        statusLabel = new JLabel("Load your moves.json and start picking skills.");

        JButton loadMovesButton = new JButton("Load Moves JSON");
        JButton generateButton = new JButton("Generate JSON");
        JButton copyButton = new JButton("Copy JSON");
        JButton editButton = new JButton("Open / Edit Existing JSON");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Demon Name:"), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);

        gbc.gridx = 2; panel.add(new JLabel("Race:"), gbc);
        gbc.gridx = 3; panel.add(raceBox, gbc);

        gbc.gridx = 4; panel.add(new JLabel("Level:"), gbc);
        gbc.gridx = 5; panel.add(levelSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Moves JSON Path:"), gbc);
        gbc.gridx = 1; panel.add(movesPathField, gbc);

        gbc.gridx = 2; panel.add(loadMovesButton, gbc);

        gbc.gridx = 3; panel.add(new JLabel("Basic Attack:"), gbc);
        gbc.gridx = 4; panel.add(basicAttackValueLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Search Moves:"), gbc);
        gbc.gridx = 1; panel.add(searchField, gbc);

        gbc.gridx = 2; panel.add(generateButton, gbc);
        gbc.gridx = 3; panel.add(copyButton, gbc);
        gbc.gridx = 4; panel.add(editButton, gbc);
        editButton.addActionListener(e -> editExistingDemon());

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 7;
        panel.add(statusLabel, gbc);

        raceBox.addActionListener(e -> updateRaceDerivedFields());
        levelSpinner.addChangeListener(e -> {
            updateRaceDerivedFields();
            updateAffinityBudget();
            recalculateLearnableSkillLevels(); // Ensure levels update if base level changes
        });

        loadMovesButton.addActionListener(this::loadMovesFromPath);
        generateButton.addActionListener(this::generateDemonJson);
        copyButton.addActionListener(e -> copyOutputToClipboard());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMoves(); }
            @Override public void removeUpdate(DocumentEvent e) { filterMoves(); }
            @Override public void changedUpdate(DocumentEvent e) { filterMoves(); }
        });

        return panel;
    }

    private JSplitPane buildCenterPanel() {
        JPanel movePanel = buildMoveBrowserPanel();
        JPanel selectionPanel = buildSelectionPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, movePanel, selectionPanel);
        split.setResizeWeight(0.60);
        return split;
    }

    private JPanel buildMoveBrowserPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Move Dictionary"));

        moveTableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Element", "Tier", "Target", "MP", "Damage", "Hit", "Tags"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        moveTable = new JTable(moveTableModel);
        moveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moveTable.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(moveTableModel);
        moveTable.setRowSorter(sorter);

        panel.add(new JScrollPane(moveTable), BorderLayout.CENTER);

        JButton addBattleButton = new JButton("Add To Battle Skills");
        addLearnableButton = new JButton("Add To Learnable Skills");
        JButton removeButton = new JButton("Remove From Selection");
        JButton reloadButton = new JButton("Reload Table");

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.add(addBattleButton);
        buttonRow.add(addLearnableButton);
        buttonRow.add(removeButton);
        buttonRow.add(reloadButton);

        panel.add(buttonRow, BorderLayout.SOUTH);

        addBattleButton.addActionListener(e -> addSelectedMoveToBattleSkills());
        addLearnableButton.addActionListener(e -> addSelectedMoveToLearnableSkills());
        removeButton.addActionListener(e -> removeSelectedSkillFromLists());
        reloadButton.addActionListener(e -> refreshMoveTable());

        return panel;
    }

    private JPanel buildSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        
        JPanel skillsPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        skillsPanel.setBorder(BorderFactory.createTitledBorder("Selected Skills"));

        battleSkillList = new JList<>(battleSkillModel);
        learnableSkillList = new JList<>(learnableSkillModel);
        battleSkillList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        learnableSkillList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        battleSkillList.setVisibleRowCount(8);
        learnableSkillList.setVisibleRowCount(8);

        JPanel battlePanel = new JPanel(new BorderLayout(6, 6));
        battlePanel.setBorder(BorderFactory.createTitledBorder("Battle Skills"));
        battlePanel.add(new JScrollPane(battleSkillList), BorderLayout.CENTER);

        JPanel learnablePanel = new JPanel(new BorderLayout(6, 6));
        learnablePanel.setBorder(BorderFactory.createTitledBorder("Learnable Skills"));
        
        JPanel capacityHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        learnCapacitySpinner = new JSpinner(new SpinnerNumberModel(4, 1, 15, 1));
        learnCapacityLabel = new JLabel();
        learnCapacityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        capacityHeader.add(new JLabel("Capacity:"));
        capacityHeader.add(learnCapacitySpinner);
        capacityHeader.add(learnCapacityLabel);
        
        learnCapacitySpinner.addChangeListener(e -> updateLearnableCapacityUI());

        learnablePanel.add(capacityHeader, BorderLayout.NORTH);
        learnablePanel.add(new JScrollPane(learnableSkillList), BorderLayout.CENTER);

        skillsPanel.add(battlePanel);
        skillsPanel.add(learnablePanel);

        JPanel affinityPanel = new JPanel(new BorderLayout(6, 6));
        affinityPanel.setBorder(BorderFactory.createTitledBorder("Affinity Budget System"));

        budgetLabel = new JLabel("Points Remaining: 0");
        budgetLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        budgetLabel.setHorizontalAlignment(SwingConstants.CENTER);
        affinityPanel.add(budgetLabel, BorderLayout.NORTH);

        affinityTableModel = new AffinityTableModel();
        affinityTableModel.addTableModelListener(e -> updateAffinityBudget());
        affinityTable = new JTable(affinityTableModel);
        affinityTable.setRowHeight(22);
        
        JComboBox<AffinityState> stateBox = new JComboBox<>(AffinityState.values());
        affinityTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(stateBox));
        
        affinityPanel.add(new JScrollPane(affinityTable), BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, skillsPanel, affinityPanel);
        verticalSplit.setResizeWeight(0.60);
        verticalSplit.setContinuousLayout(true);
        verticalSplit.setBorder(null);

        panel.add(verticalSplit, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane buildBottomPanel() {
        jsonOutputArea = new JTextArea(12, 100);
        jsonOutputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        jsonOutputArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(jsonOutputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Generated JSON"));
        return scrollPane;
    }

    // ==================================
    // CAPACITY & BUDGET LOGIC
    // ==================================

    private void updateLearnableCapacityUI() {
        int current = learnableSkillModel.size();
        int max = (Integer) learnCapacitySpinner.getValue();
        
        learnCapacityLabel.setText(String.format("Skills: %d / %d", current, max));

        if (current >= max) {
            learnCapacityLabel.setForeground(Color.RED);
            if (addLearnableButton != null) addLearnableButton.setEnabled(false);
        } else {
            learnCapacityLabel.setForeground(new Color(0, 153, 0));
            if (addLearnableButton != null) addLearnableButton.setEnabled(true);
        }
    }

    private int getMaxAffinityBudget(int level) {
        if (level < 15) return -1;
        if (level < 35) return 0;
        if (level < 60) return 2;
        return 5;
    }

    private void updateAffinityBudget() {
        int level = (Integer) levelSpinner.getValue();
        int maxBudget = getMaxAffinityBudget(level);
        int spent = affinityTableModel.calculateTotalCost();
        int remaining = maxBudget - spent;

        budgetLabel.setText(String.format("Affinity Budget: %d  |  Cost: %d  |  Remaining: %d", maxBudget, spent, remaining));
        
        if (remaining < 0) {
            budgetLabel.setForeground(Color.RED);
            budgetLabel.setText(budgetLabel.getText() + " (OVER BUDGET!)");
        } else {
            budgetLabel.setForeground(new Color(0, 153, 0)); 
        }
    }

    private LinkedHashMap<String, String> buildAffinitiesFromGrid() {
        LinkedHashMap<String, String> affinities = new LinkedHashMap<>();
        for (int i = 0; i < affinityTableModel.getRowCount(); i++) {
            String element = affinityTableModel.getElementAt(i);
            AffinityState state = affinityTableModel.getStateAt(i);
            affinities.put(element, state.jsonName);
        }
        return affinities;
    }

    // ==================================
    // FILE / EVENT ACTIONS
    // ==================================

    private void loadMovesFromPath(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load moves.json");
        chooser.setSelectedFile(new File(movesPathField.getText().trim()));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        movesPathField.setText(file.getAbsolutePath());

        try {
            moveRepository.load(file);
            refreshMoveTable();
            statusLabel.setText("Loaded " + moveRepository.moves.size() + " moves.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load moves JSON:\n" + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMoveTable() {
        moveTableModel.setRowCount(0);
        for (MoveDefinition move : moveRepository.moves) {
            moveTableModel.addRow(new Object[]{
                    move.id, move.name, move.element, move.tier, move.targetType,
                    move.mpCost, move.baseDamage, move.hitRate,
                    move.tags == null ? "" : String.join(", ", move.tags)
            });
        }
        filterMoves();
    }

    private void filterMoves() {
        if (sorter == null) return;
        String text = searchField.getText().trim();
        if (text.isEmpty()) { sorter.setRowFilter(null); return; }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    private void addSelectedMoveToBattleSkills() {
        MoveDefinition move = getSelectedMove();
        if (move == null || containsSkill(battleSkillModel, move.id)) return;
        battleSkillModel.addElement(new SkillPick(move.id, move.name, -1));
    }

    private void addSelectedMoveToLearnableSkills() {
        MoveDefinition move = getSelectedMove();
        if (move == null) return;

        int max = (Integer) learnCapacitySpinner.getValue();
        if (learnableSkillModel.size() >= max) {
            JOptionPane.showMessageDialog(this, "Learnable skill capacity reached. Remove a skill first.", "Capacity Full", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (containsSkill(learnableSkillModel, move.id)) {
            JOptionPane.showMessageDialog(this, "Skill already exists in the learnset.", "Duplicate Skill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int baseLevel = (Integer) levelSpinner.getValue();
        int newLevel = baseLevel + learnableSkillModel.size() + 1;
        learnableSkillModel.addElement(new SkillPick(move.id, move.name, newLevel));
        
        recalculateLearnableSkillLevels();
        updateLearnableCapacityUI(); // Re-evaluate capacity
    }

    private void removeSelectedSkillFromLists() {
        int battleIndex = battleSkillList.getSelectedIndex();
        if (battleIndex >= 0) { 
            battleSkillModel.remove(battleIndex); 
            battleSkillList.clearSelection();
        }
        
        int learnIndex = learnableSkillList.getSelectedIndex();
        if (learnIndex >= 0) {
            learnableSkillModel.remove(learnIndex);
            learnableSkillList.clearSelection();
            recalculateLearnableSkillLevels(); // Slide existing skills down to fill the gap sequentially
            updateLearnableCapacityUI(); // Re-evaluate capacity
        }
    }

    private void recalculateLearnableSkillLevels() {
        int baseLevel = (Integer) levelSpinner.getValue();
        for (int i = 0; i < learnableSkillModel.size(); i++) {
            SkillPick pick = learnableSkillModel.get(i);
            pick.level = baseLevel + i + 1;
        }
        if (learnableSkillList != null) {
            learnableSkillList.repaint(); // Force visual update in case levels shifted
        }
    }

    private MoveDefinition getSelectedMove() {
        int row = moveTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a move from the grid first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return moveRepository.findById(String.valueOf(moveTableModel.getValueAt(moveTable.convertRowIndexToModel(row), 0)));
    }

    private boolean containsSkill(DefaultListModel<SkillPick> model, String skillId) {
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i).skillId.equals(skillId)) return true;
        }
        return false;
    }

    private void updateRaceDerivedFields() {
        RaceDefinition race = (RaceDefinition) raceBox.getSelectedItem();
        if (race != null) basicAttackValueLabel.setText(race.basicAttackId);
    }

    private void generateDemonJson(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a demon name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int level = (Integer) levelSpinner.getValue();
        int maxBudget = getMaxAffinityBudget(level);
        int spent = affinityTableModel.calculateTotalCost();
        
        if (spent > maxBudget) {
            JOptionPane.showMessageDialog(this, 
                "You are over the Affinity Budget! \n\nPlease lower some affinities or increase the demon's base level.", 
                "Over Budget Restriction", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        RaceDefinition race = (RaceDefinition) raceBox.getSelectedItem();
        String id = slugify(name);

        Stats stats = calculateStats(race, level);
        int hp = (level + stats.vit) * 6;
        int mp = (level + stats.mag) * 3;

        List<String> battleSkills = new ArrayList<>();
        if (race.basicAttackId != null && !race.basicAttackId.isBlank()) battleSkills.add(race.basicAttackId);
        
        for (int i = 0; i < battleSkillModel.size(); i++) {
            String skillId = battleSkillModel.get(i).skillId;
            if (!battleSkills.contains(skillId)) battleSkills.add(skillId);
        }

        List<LearnableSkill> learnableSkills = new ArrayList<>();
        for (int i = 0; i < learnableSkillModel.size(); i++) {
            learnableSkills.add(new LearnableSkill(learnableSkillModel.get(i).level, learnableSkillModel.get(i).skillId));
        }

        GeneratedDemon demon = new GeneratedDemon();
        demon.id = id;
        demon.name = name;
        demon.race = race.displayName;
        demon.base_level = level;
        demon.max_skill_level = level + ((level < 40) ? 4 : (level < 70) ? 5 : 6); // Retained legacy scaling
        demon.level = level;
        demon.hp = hp;
        demon.mp = mp;
        demon.basicAttack = race.basicAttackId;
        demon.stats = stats;
        demon.affinities = buildAffinitiesFromGrid();
        demon.battleSkills = battleSkills;
        demon.learnableSkills = learnableSkills;

        JsonObject generated = gson.toJsonTree(demon).getAsJsonObject();
        generated.addProperty("schemaVersion", 1);
        generated.addProperty("learnableCapacity", (Integer) learnCapacitySpinner.getValue());
        try { documents.parse(gson.toJson(generated), "generated demon"); }
        catch (IllegalArgumentException invalid) { JOptionPane.showMessageDialog(this, invalid.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE); return; }
        jsonOutputArea.setText(documents.format(generated));
        jsonOutputArea.setCaretPosition(0);
        statusLabel.setText("Generated JSON for " + name + " (" + race.displayName + ")");
    }

    private void editExistingDemon() {
        JFileChooser chooser = new JFileChooser(System.getProperty("guise.assets", "assets"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.nio.file.Path original = chooser.getSelectedFile().toPath();
        try {
            JTextArea editor = new JTextArea(documents.format(documents.open(original)), 30, 90);
            editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            while (true) {
                int option = JOptionPane.showOptionDialog(this, new JScrollPane(editor), "Edit existing demon (unknown fields retained)",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Validate / Preview", "Save As", "Close"}, "Validate / Preview");
                if (option < 0 || option == 2) return;
                try {
                    JsonObject raw = documents.parse(editor.getText(), original.toString());
                    if (option == 0) {
                        List<String> warnings = documents.runtimeDiagnostics(raw);
                        JOptionPane.showMessageDialog(this, warnings.isEmpty() ? "Valid content; selected moves supported by slice." : "Content preserved; deferred runtime behavior:\n" + String.join("\n", warnings));
                    } else {
                        chooser.setSelectedFile(original.toFile());
                        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                            java.nio.file.Path destination = chooser.getSelectedFile().toPath();
                            if (java.nio.file.Files.exists(destination) && JOptionPane.showConfirmDialog(this, "Replace " + destination + "?", "Save", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) continue;
                            documents.save(destination, editor.getText());
                            statusLabel.setText("Saved " + destination + " with all JSON fields retained.");
                        }
                    }
                } catch (Exception invalid) { JOptionPane.showMessageDialog(this, invalid.getMessage(), "Content error", JOptionPane.ERROR_MESSAGE); }
            }
        } catch (Exception invalid) { JOptionPane.showMessageDialog(this, invalid.getMessage(), "Open failed", JOptionPane.ERROR_MESSAGE); }
    }

    private void copyOutputToClipboard() {
        String text = jsonOutputArea.getText().trim();
        if (text.isEmpty()) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "JSON copied to clipboard.");
    }

    private Stats calculateStats(RaceDefinition race, int level) {
        int totalPool = level + 20;
        int[] values = new int[5];
        double[] remainders = new double[5];
        int sum = 0;

        for (int i = 0; i < 5; i++) {
            double raw = totalPool * race.statWeights[i];
            values[i] = (int) Math.floor(raw);
            remainders[i] = raw - values[i];
            sum += values[i];
        }

        for (int remaining = totalPool - sum; remaining > 0; remaining--) {
            int index = 0;
            double best = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < remainders.length; i++) {
                if (remainders[i] > best) { best = remainders[i]; index = i; }
            }
            values[index]++;
            remainders[index] = -1;
        }
        return new Stats(values[0], values[1], values[2], values[3], values[4]);
    }

    private String slugify(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new DemonConfig().setVisible(true);
        });
    }

    // =========================================
    // INNER CLASSES & DATA MODELS
    // =========================================

    public enum AffinityState {
        WEAK("Weak", -1),
        NORMAL("Neutral", 0),
        RESIST("Resist", 1),
        NULL("Null", 2),
        DRAIN("Drain", 3),
        REPEL("Repel", 3);

        public final String jsonName;
        public final int cost;

        AffinityState(String jsonName, int cost) {
            this.jsonName = jsonName;
            this.cost = cost;
        }

        @Override
        public String toString() {
            return jsonName + " (" + (cost > 0 ? "+" : "") + cost + ")";
        }
    }

    static class AffinityTableModel extends AbstractTableModel {
        private final String[] columns = {"Element", "State"};
        private final String[] elements = {"phys", "soul", "modal", "fusion", "bebop", "harmony", "dissonance"};
        private final AffinityState[] states;

        public AffinityTableModel() {
            states = new AffinityState[elements.length];
            Arrays.fill(states, AffinityState.NORMAL); 
        }

        @Override public int getRowCount() { return elements.length; }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnIndex == 0 ? elements[rowIndex] : states[rowIndex];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1 && aValue instanceof AffinityState) {
                states[rowIndex] = (AffinityState) aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return columnIndex == 1; }

        public String getElementAt(int rowIndex) { return elements[rowIndex]; }
        public AffinityState getStateAt(int rowIndex) { return states[rowIndex]; }

        public int calculateTotalCost() {
            int total = 0;
            for (AffinityState state : states) total += state.cost;
            return total;
        }
    }

    static class MoveRepository {
        private final Gson gson = new Gson();
        private final List<MoveDefinition> moves = new ArrayList<>();

        void load(File file) throws Exception {
            try (Reader reader = new FileReader(file)) {
                moves.clear();
                JsonElement root = JsonParser.parseReader(reader);
                
                if (root.isJsonArray()) {
                    MoveDefinition[] array = gson.fromJson(root, MoveDefinition[].class);
                    if (array != null) moves.addAll(Arrays.asList(array));
                } else if (root.isJsonObject()) {
                    JsonObject obj = root.getAsJsonObject();
                    if (obj.has("moves") && obj.get("moves").isJsonArray()) {
                        MoveDefinition[] array = gson.fromJson(obj.get("moves"), MoveDefinition[].class);
                        if (array != null) moves.addAll(Arrays.asList(array));
                    } else {
                        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                            MoveDefinition md = gson.fromJson(entry.getValue(), MoveDefinition.class);
                            if (md.id == null) md.id = entry.getKey(); 
                            moves.add(md);
                        }
                    }
                }
            }
        }

        MoveDefinition findById(String id) {
            for (MoveDefinition move : moves) {
                if (move.id != null && move.id.equals(id)) return move;
            }
            return null;
        }
    }

    static class MoveDefinition {
        String id; String name; String element; String tier; String targetType;
        int mpCost; int baseDamage; float hitRate = 1.0f;
        List<String> tags = new ArrayList<>();
        StatusEffect statusEffect; List<StatusEffect> statusEffects;
        Double instantKillChance; String instrumentType; String scalesWith;
        Integer hitsMin; Integer hitsMax; Double critRate; Double hpReductionPercent;
    }

    static class StatusEffect { String type; double chance; }
    static class SkillPick {
        String skillId; String skillName; int level;
        SkillPick(String id, String name, int lvl) { skillId = id; skillName = name; level = lvl; }
        @Override public String toString() { return level >= 0 ? "Lv " + level + " — " + skillName : skillName; }
    }

    static class GeneratedDemon {
        String id; String name; String race; int level; int base_level; int max_skill_level;
        int hp; int mp; String basicAttack; Stats stats;
        LinkedHashMap<String, String> affinities;
        List<String> battleSkills; List<LearnableSkill> learnableSkills;
    }

    static class LearnableSkill {
        int level; String skill;
        LearnableSkill(int level, String skill) { this.level = level; this.skill = skill; }
    }

    static class Stats {
        int str; int vit; int mag; int agi; int luk;
        Stats(int str, int vit, int mag, int agi, int luk) {
            this.str = str; this.vit = vit; this.mag = mag; this.agi = agi; this.luk = luk;
        }
    }

}
