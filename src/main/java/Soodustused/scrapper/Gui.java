package Soodustused.scrapper;

import Soodustused.SoodustusedApplication;
import Soodustused.scrapper.Scrapper.ShopConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Gui extends JFrame {
    List<ShopConfiguration> configs;

    private JPanel sidePanel;
    private JPanel entryPanel;
    private JButton confirmButton;
    private JButton cancelButton;
    private JButton startButton;

    private JTextField urlTextField;
    private JTextField imageTextField;
    private JTextField itemNameTextField;
    private JTextField origPriceTextField;
    private JTextField discountPriceTextField;

    private JTextField urlAttributeField;
    private JTextField imageAttributeField;
    private JTextField itemNameAttributeField;
    private JTextField origPriceAttributeField;
    private JTextField discountPriceAttributeField;


    private JCheckBox urlCheckbox;
    private JCheckBox imageCheckbox;
    private JCheckBox itemNameCheckbox;
    private JCheckBox origPriceCheckbox;
    private JCheckBox discountPriceCheckbox;

    public Gui(List<ShopConfiguration> configs) {
        this.configs = configs;

        setMinimumSize(new Dimension(1200, 600));
        setTitle("Scrapper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        EmptyBorder emptyBorder = new EmptyBorder(15, 10, 0, 10);

        sidePanel.setBorder(emptyBorder);

        JLabel nameLabel = new JLabel("Shop Name:");
        JTextField nameTextField = createTextField("Shop");
        sidePanel.add(createParameterPanel(nameLabel, nameTextField));

        JLabel urlLabel = new JLabel("URL to Scrape:");
        JTextField urlScrapeTextField = createTextField("https://google.com");
        sidePanel.add(createParameterPanel(urlLabel, urlScrapeTextField));

        JLabel itemSelectorLabel = new JLabel("Item Selector:");
        JTextField itemSelectorTextField = createTextField("ul > li.item");
        sidePanel.add(createParameterPanel(itemSelectorLabel, itemSelectorTextField));

        JLabel cookieSelectorLabel = new JLabel("Cookie Selectors:");
        JTextField cookieSelectorTextField = createTextField("button.cookie-alert-decline-button, button.overlay__closer");
        sidePanel.add(createParameterPanel(cookieSelectorLabel, cookieSelectorTextField));

        JPanel customPropertiesPanel = new JPanel();
        customPropertiesPanel.setLayout(new BoxLayout(customPropertiesPanel, BoxLayout.X_AXIS));

        customPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Custom Properties"));

        List<JCheckBox> checkBoxes = new ArrayList<>();
        urlCheckbox = new JCheckBox("Url");
        imageCheckbox = new JCheckBox("Image");
        itemNameCheckbox = new JCheckBox("Item Name");
        origPriceCheckbox = new JCheckBox("Original Price");
        discountPriceCheckbox = new JCheckBox("Discount Price");

        customPropertiesPanel.add(urlCheckbox);
        customPropertiesPanel.add(imageCheckbox);
        customPropertiesPanel.add(itemNameCheckbox);
        customPropertiesPanel.add(origPriceCheckbox);
        customPropertiesPanel.add(discountPriceCheckbox);

        sidePanel.add(customPropertiesPanel);

        JPanel propertiesPanel = new JPanel(new GridLayout(0, 1));
        propertiesPanel.setBorder(BorderFactory.createTitledBorder("Properties"));

        JPanel urlPropertyPanel = addProperty("URL", "a.url", "href");
        urlTextField = (JTextField) ((JPanel) urlPropertyPanel.getComponent(0)).getComponent(1);
        urlAttributeField = (JTextField) ((JPanel) urlPropertyPanel.getComponent(1)).getComponent(1);

        JPanel imagePropertyPanel = addProperty("Image", "div.image > img", "src");
        imageTextField = (JTextField) ((JPanel) imagePropertyPanel.getComponent(0)).getComponent(1);
        imageAttributeField = (JTextField) ((JPanel) imagePropertyPanel.getComponent(1)).getComponent(1);

        JPanel itemNamePropertyPanel = addProperty("Item Name", "div.name", "text");
        itemNameTextField = (JTextField) ((JPanel) itemNamePropertyPanel.getComponent(0)).getComponent(1);
        itemNameAttributeField = (JTextField) ((JPanel) itemNamePropertyPanel.getComponent(1)).getComponent(1);

        JPanel origPricePropertyPanel = addProperty("Original Price", "span.price", "text");
        origPriceTextField = (JTextField) ((JPanel) origPricePropertyPanel.getComponent(0)).getComponent(1);
        origPriceAttributeField = (JTextField) ((JPanel) origPricePropertyPanel.getComponent(1)).getComponent(1);

        JPanel discountPricePropertyPanel = addProperty("Discount Price", "div.price > span", "text");
        discountPriceTextField = (JTextField) ((JPanel) discountPricePropertyPanel.getComponent(0)).getComponent(1);
        discountPriceAttributeField = (JTextField) ((JPanel) discountPricePropertyPanel.getComponent(1)).getComponent(1);

        propertiesPanel.add(urlPropertyPanel);
        propertiesPanel.add(imagePropertyPanel);
        propertiesPanel.add(itemNamePropertyPanel);
        propertiesPanel.add(origPricePropertyPanel);
        propertiesPanel.add(discountPricePropertyPanel);

        propertiesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        sidePanel.add(propertiesPanel);



        String[] columnNames = {"Shop Name", "Scrape URL", "Item Selector", "Cookie Selectors", "Url", "Image", "Item Name", "Original Price", "Discount Price", "Url Text", "Url Attribute", "Image Text", "Image Attribute", "Item Name", "Item Attribute", "Original Price Text", "Original Price Attribute", "Discount Price Text", "Discount Price Attribute"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4 || columnIndex == 5 ||columnIndex == 6 ||columnIndex == 7 ||columnIndex == 8) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        loadTableData(tableModel, configs);
        JTable table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(4).setCellRenderer(new CheckboxRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new CheckboxRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new CheckboxRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new CheckboxRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new CheckboxRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int rowIndex = table.rowAtPoint(e.getPoint());

                    if (rowIndex != -1) {
                        table.setRowSelectionInterval(rowIndex, rowIndex);

                        JPopupMenu contextMenu = createContextMenu(table);
                        contextMenu.show(table, e.getX(), e.getY());
                    }
                }
            }
        });

        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            TableColumn column = table.getColumnModel().getColumn(columnIndex);

            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            Object headerValue = column.getHeaderValue();
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, headerValue, false, false, 0, 0);
            int headerWidth = headerComp.getPreferredSize().width + 10;
            column.setMinWidth(headerWidth);


            int preferredWidth = getColumnPreferredWidth(table, columnIndex);
            column.setPreferredWidth(preferredWidth);
        }

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetTextField(nameTextField);
                resetTextField(urlScrapeTextField);
                resetTextField(itemSelectorTextField);
                resetTextField(cookieSelectorTextField);

                urlCheckbox.setSelected(false);
                imageCheckbox.setSelected(false);
                itemNameCheckbox.setSelected(false);
                origPriceCheckbox.setSelected(false);
                discountPriceCheckbox.setSelected(false);

                resetTextField(urlTextField);
                resetTextField(imageTextField);
                resetTextField(itemNameTextField);
                resetTextField(origPriceTextField);
                resetTextField(discountPriceTextField);

                resetTextField(urlAttributeField);
                resetTextField(imageAttributeField);
                resetTextField(itemNameAttributeField);
                resetTextField(origPriceAttributeField);
                resetTextField(discountPriceAttributeField);
            }
        });

        confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String shopName = fieldGetText(nameTextField);
                if (shopName == null) {
                    JOptionPane.showMessageDialog(null, "Shop Name is required field!");
                    return;
                }
                String url = fieldGetText(urlScrapeTextField);
                String itemSelector = fieldGetText(itemSelectorTextField);
                String cookieSelectorsText = fieldGetText(cookieSelectorTextField);

                String[] cookieSelectors = new String[]{};
                if (cookieSelectorsText != null) {
                    cookieSelectors = cookieSelectorsText.split(", ");
                }
                boolean urlChecked = urlCheckbox.isSelected();
                boolean imageChecked = imageCheckbox.isSelected();
                boolean itemNameChecked = itemNameCheckbox.isSelected();
                boolean origPriceChecked = origPriceCheckbox.isSelected();
                boolean discountPriceChecked = discountPriceCheckbox.isSelected();

                String urlPropertyText = fieldGetText(urlTextField);
                String imagePropertyText = fieldGetText(imageTextField);
                String itemNamePropertyText = fieldGetText(itemNameTextField);
                String origPricePropertyText = fieldGetText(origPriceTextField);
                String discountPricePropertyText = fieldGetText(discountPriceTextField);

                String urlPropertyAttribute = fieldGetText(urlAttributeField);
                String imagePropertyAttribute = fieldGetText(imageAttributeField);
                String itemNamePropertyAttribute = fieldGetText(itemNameAttributeField);
                String origPricePropertyAttribute = fieldGetText(origPriceAttributeField);
                String discountPricePropertyAttribute = fieldGetText(discountPriceAttributeField);

                ShopConfiguration newConfig = new ShopConfiguration();
                newConfig.setShopName(shopName);
                newConfig.setScrapeUrl(url);
                newConfig.setItemSelector(itemSelector);
                newConfig.setCookieSelectors(cookieSelectors);

                List<String> customProperties = new ArrayList<>();
                if (urlChecked) {
                    customProperties.add("url");
                }
                if (imageChecked) {
                    customProperties.add("image");
                }
                if (itemNameChecked) {
                    customProperties.add("name");
                }
                if (origPriceChecked) {
                    customProperties.add("origPrice");
                }
                if (discountPriceChecked) {
                    customProperties.add("price");
                }
                newConfig.setCustomLogicProperties(customProperties.toArray(new String[0]));

                Map<String, String[]> propertyAttributeSelectors = new HashMap<>();
                propertyAttributeSelectors.put("url", new String[]{urlPropertyText, urlPropertyAttribute});
                propertyAttributeSelectors.put("image", new String[]{imagePropertyText, imagePropertyAttribute});
                propertyAttributeSelectors.put("name", new String[]{itemNamePropertyText, itemNamePropertyAttribute});
                propertyAttributeSelectors.put("origPrice", new String[]{origPricePropertyText, origPricePropertyAttribute});
                propertyAttributeSelectors.put("price", new String[]{discountPricePropertyText, discountPricePropertyAttribute});
                newConfig.setProperties(propertyAttributeSelectors);

                configs.add(newConfig);

                saveConfiguration(configs);

                loadTableData(tableModel, configs);

                JOptionPane.showMessageDialog(null, "Data saved successfully!");
            }
        });

        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                SpringApplication.run(SoodustusedApplication.class);
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        buttonsPanel.add(Box.createHorizontalGlue());

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(confirmButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(startButton);
        buttonsPanel.add(Box.createHorizontalGlue());
        sidePanel.add(buttonsPanel);

        JScrollPane scrollPane = new JScrollPane(table);

        entryPanel = new JPanel(new GridLayout(0, 1));
        entryPanel.add(scrollPane);
        add(sidePanel, BorderLayout.WEST);
        add(entryPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private String fieldGetText(JTextField field) {
        if (field.getForeground().equals(Color.GRAY)) return null;
        else return field.getText();
    }

    private void loadTableData(DefaultTableModel tableModel, List<ShopConfiguration> configs) {
        tableModel.setRowCount(0);
        for (ShopConfiguration config : configs) {
            String[] rawCookieSelectors = config.getCookieSelectors();
            String cookieSelectors = "";
            if (rawCookieSelectors != null && rawCookieSelectors.length > 0) {
                cookieSelectors = String.join(", ", rawCookieSelectors);
            }

            Map<String, Boolean> customProperties = new HashMap<>();
            String[] rawCustomProperties = config.getCustomLogicProperties();
            if (rawCustomProperties != null) {
                customProperties = Arrays.stream(rawCustomProperties)
                        .filter(property -> !property.isEmpty())
                        .collect(Collectors.toMap(property -> property, property -> true));
            }

            Map<String, String[]> propertyAttributeSelectors = new HashMap<>();
            Map<String, String[]> properties = config.getProperties();

            for (Map.Entry<String, String[]> entry : properties.entrySet()) {
                String property = entry.getKey();
                String[] rawSelectors = entry.getValue();

                String selectors = null, attribute = null;
                if (rawSelectors != null && rawSelectors.length > 0) {
                    StringBuilder selectorsBuilder = new StringBuilder();
                    for (int i = 0; i < rawSelectors.length - 1; i++) {
                        if (rawSelectors[i] != null) {
                            selectorsBuilder.append(rawSelectors[i]);
                        }
                    }
                    selectors = selectorsBuilder.toString();
                    attribute = rawSelectors[rawSelectors.length - 1];
                }
                propertyAttributeSelectors.put(property, new String[]{selectors, attribute});
            }

            Object[] rowData = {
                    config.getShopName(),
                    config.getScrapeUrl(),
                    config.getItemSelector(),
                    cookieSelectors,
                    customProperties.get("url"),
                    customProperties.get("image"),
                    customProperties.get("name"),
                    customProperties.get("origPrice"),
                    customProperties.get("price"),
                    propertyAttributeSelectors.get("url")[0],
                    propertyAttributeSelectors.get("url")[1],
                    propertyAttributeSelectors.get("image")[0],
                    propertyAttributeSelectors.get("image")[1],
                    propertyAttributeSelectors.get("name")[0],
                    propertyAttributeSelectors.get("name")[1],
                    propertyAttributeSelectors.get("origPrice")[0],
                    propertyAttributeSelectors.get("origPrice")[1],
                    propertyAttributeSelectors.get("price")[0],
                    propertyAttributeSelectors.get("price")[1],

            };
            tableModel.addRow(rowData);
        }
    }

    private JPopupMenu createContextMenu(JTable table) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow != -1) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                String shopName = (String) model.getValueAt(selectedRow, 0);

                model.removeRow(selectedRow);
                configs.removeIf(config -> config.getShopName().equals(shopName));
                saveConfiguration(configs);
                Scrapper.updateShopConfigs();
            }
        });

        contextMenu.add(deleteItem);
        saveConfiguration(configs);
        Scrapper.updateShopConfigs();
        return contextMenu;
    }

    private static void saveConfiguration(List<ShopConfiguration> shopsConfigurations) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Path outputPath = Paths.get("json/shopsConfig.json");
            Files.createDirectories(outputPath.getParent());
            objectMapper.writeValue(outputPath.toFile(), shopsConfigurations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int getColumnPreferredWidth(JTable table, int columnIndex) {
        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        int maxWidth = 0;

        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            TableCellRenderer renderer = table.getCellRenderer(rowIndex, columnIndex);
            Component comp = table.prepareRenderer(renderer, rowIndex, columnIndex);
            maxWidth = Math.max(comp.getPreferredSize().width, maxWidth);
        }

        return maxWidth;
    }

    static class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setSelected((value != null && (Boolean) value));
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return this;
        }
    }

    private void scrapeShopsInAThread() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Scrapper.scrapeShops();
                return null;
            }

            @Override
            protected void done() {
                startButton.setEnabled(true);
            }
        };

        worker.execute();
    }

    private JPanel createParameterPanel(JLabel label, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        panel.add(label, BorderLayout.NORTH);
        panel.add(label, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel addProperty(String propertyName, String namePlaceholder, String attributePlaceholder) {
        JLabel propertyLabel = new JLabel(propertyName);
        JTextField propertyTextField = createTextField(namePlaceholder);

        JLabel attributeLabel = new JLabel("Attribute");
        JTextField attributeTextField = createTextField(attributePlaceholder);

        JPanel propertyPanel = new JPanel(new GridLayout(2, 1));
        propertyPanel.add(propertyLabel);
        propertyPanel.add(propertyTextField);

        JPanel attributePanel = new JPanel(new GridLayout(2, 1));
        attributePanel.add(attributeLabel);
        attributePanel.add(attributeTextField);

        JPanel propertyAttributePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        propertyAttributePanel.add(propertyPanel);
        propertyAttributePanel.add(attributePanel);

        return propertyAttributePanel;
    }

    private JTextField createTextField (String placeholder) {
        JTextField textField = new JTextField();
        setPlaceholder(placeholder, textField);

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(getPlaceholder(textField)) &&
                textField.getForeground().equals(Color.GRAY)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
        return textField;
    }

    private void setPlaceholder(String placeholder, JTextField textField) {
        textField.putClientProperty("placeholder", placeholder);
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
    }

    private String getPlaceholder(JTextField textField) {
        return (String) textField.getClientProperty("placeholder");
    }

    private void resetTextField(JTextField textField) {
        String placeholder = getPlaceholder(textField);
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
    }

    public void showGui() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }
}