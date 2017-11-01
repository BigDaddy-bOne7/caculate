package calculate;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import meta.Config;
import meta.Deliver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Utils.getDao;

/**
 * Created by yangz on 2017/8/31 17:08.
 */
public class Calculate {
    private JFrame mainFrame;
    private JPanel filePanel;
    private JLabel statusLabel;
    private JPanel controlPanel;
    private JFrame configFrame;


    private Calculate() {
        prepareGUI();
    }

    //main方法
    public static void main(String[] args) throws Exception {
        Calculate swingControlDemo = new Calculate();
        swingControlDemo.showCalculate();
    }

    //主界面参数
    private void prepareGUI() {
        mainFrame = new JFrame("快递运费计算器");
        mainFrame.setSize(480, 400);
        mainFrame.setLayout(new GridLayout(4, 1));
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        filePanel = new JPanel();
        filePanel.setLayout(new GridLayout(2, 1));

        statusLabel = new JLabel("status", JLabel.CENTER);

        mainFrame.add(new JLabel("选择快递", JLabel.CENTER));
        mainFrame.add(controlPanel);
        mainFrame.add(filePanel);
        mainFrame.add(statusLabel);
        mainFrame.setVisible(true);
    }

    //主界面初始化
    private void showCalculate() throws Exception {

        final DefaultComboBoxModel<String> deliversName = new DefaultComboBoxModel<>();
        //获取快递列表
        List<Deliver> deliverList = getDao().getDeliverList();
        //遍历快递列表，添加到主界面下拉菜单中
        for (Deliver deliver : deliverList) deliversName.addElement(deliver.getName());

        final JComboBox<String> deliverCombo = new JComboBox<>(deliversName);
        deliverCombo.setSelectedIndex(0);

        JScrollPane deliverListScrollPane = new JScrollPane(deliverCombo);
        //添加设置按钮
        JButton showButton = new JButton("设置");

        showButton.addActionListener(e -> {
            if (deliverCombo.getSelectedIndex() != -1) {
                try {
                    showConfig(deliverCombo.getItemAt(deliverCombo.getSelectedIndex()));
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        JPanel fileChoosePanel = new JPanel();
        fileChoosePanel.setLayout(new FlowLayout());
        JFileChooser fileDialog = new JFileChooser();
        JButton showFileDialogButton = new JButton("选择文件");
        JButton runButton = new JButton("开始计算");

        showFileDialogButton.addActionListener(e -> {
            int returnVal = fileDialog.showOpenDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileDialog.getSelectedFile();
                statusLabel.setText("选择了文件 :"
                        + file.getName());
            } else {
                statusLabel.setText("已取消文件选择");
            }
        });

        //计算
        runButton.addActionListener((ActionEvent e) -> {
            File file = fileDialog.getSelectedFile();
            String error1 = null;
            File newFile = new File("D://1.xls");
            try {
                copyFile(file, newFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if ("xls".equals(file.getName().substring(file.getName().lastIndexOf(".") + 1))) {
                try {
                    Workbook book = Workbook.getWorkbook(newFile);
                    WritableWorkbook writableBook = Workbook.createWorkbook(newFile, book);
                    WritableSheet sheet = writableBook.getSheet(0);
                    String deliverName = deliverCombo.getItemAt(deliverCombo.getSelectedIndex());
                    for (int i = 1; i < sheet.getRows(); i++) {
                        double fee = 0;
                        try {
                            String province = sheet.getCell(0, i).getContents();
                            String standardProvince = getDao().getStandardProvince(province);
                            List<Config> lc = getDao().getCalculateConfigs(deliverName, standardProvince);
                            double weight = Double.valueOf(sheet.getCell(1, i).getContents());
                            for (Config config : lc) {
                                double min = config.getMin();
                                double max = config.getMax();
                                double unit = config.getUnit();
                                double price = config.getPrice();
                                double totalPrice = config.getTotalPrice();
                                if (max == 0 && weight > min) {
                                    if (unit != 0 && price != 0)
                                        fee += ((weight - min) % unit == 0) ? (weight - min) / unit * price : (int)((weight - min) / unit / 1 + 1) * price;
                                    else
                                        fee += totalPrice;
                                } else if (max != 0 && weight > max) {
                                    if (unit != 0 && price != 0)
                                        fee += ((max - min) % unit == 0) ? (max - min) / unit * price : (int)((max - min) / unit + 1) * price;
                                    else
                                        fee += totalPrice;
                                } else if (weight > min && weight <= max) {
                                    if (unit != 0 && price != 0)
                                        fee += ((weight - min) % unit == 0) ? (weight - min) / unit * price : (int)((weight - min) / unit + 1) * price;
                                    else
                                        fee += totalPrice;
                                }
                            }
                            sheet.addCell(new Label(2, i, fee + ""));
                        } catch (Exception e2) {
                            error1 = "第" + (i + 1) + "行数据有误";
                            break;
                        }
                    }
                    writableBook.write();
                    writableBook.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            } else error1 = "文件格式不对，请选择后缀名为‘.xls’的文件";
            if (error1 != null) {
                int output1 = JOptionPane.showConfirmDialog(mainFrame
                        , error1
                        , "error"
                        , JOptionPane.DEFAULT_OPTION);
            } else {
                try {
                    copyFile(newFile, file);
                    int output2 = JOptionPane.showConfirmDialog(mainFrame
                            , "计算完成"
                            , "计算完成"
                            , JOptionPane.DEFAULT_OPTION);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }


        });

        fileChoosePanel.add(showFileDialogButton);
        fileChoosePanel.add(runButton);
        filePanel.add(fileChoosePanel);

        controlPanel.add(deliverListScrollPane);
        controlPanel.add(showButton);
        mainFrame.setVisible(true);
    }

    //设置界面参数
    private void showConfig(String deliverName) throws Exception {
        //设置新页面标题
        configFrame = new JFrame("快递设置");

        //新页面大小
        configFrame.setSize(1024, 768);
        //获取已有条件列表
        List<Config> configList = getDao().getConfigs(deliverName);
        //设置layout模板布局
        configFrame.setLayout(new GridLayout(configList.size() + 3, 2));
        //添加第一行标题到layout
        configFrame.add(new JLabel(deliverName + "设置", JLabel.CENTER));
        int deliverId = getDao().getDeliverId(deliverName);
        //遍历条件列表，组装成字符串
        for (Config config : configList) {
            JPanel jPanel = new JPanel();
            jPanel.setLayout(new FlowLayout());
            JTextArea jTextArea = new JTextArea();
            String weight;
            String Price;
            String Area;
            if (config.getMin() == 0) weight = "首重" + config.getMax() + "千克以内,";
            else if (config.getMax() == 0) weight = "超过" + config.getMin() + "千克后,";
            else weight = "大于" + config.getMin() + "千克,小于" + config.getMax() + "千克,";
            if (config.getTotalPrice() != 0) Price = "共计" + config.getTotalPrice() + "元。 ";
            else Price = "每" + config.getUnit() + "千克" + config.getPrice() + "元。 ";
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("地区：");
            List<String> areas = getDao().getShortArea(config.getId());
            for (String area : areas) stringBuilder.append(area).append(";");
            Area = stringBuilder.toString();
            //将条件字符串添加到textarea
            jTextArea.setRows(2);
            jTextArea.append(weight + Price + "\r\n");
            jTextArea.append(Area);
            //添加textArea到Panel
            jPanel.add(jTextArea);

            //添加编辑按钮
            JButton editButton = new JButton("编辑");
            editButton.addActionListener(e -> {
                try {
                    System.out.println("config.getId()+deliverName的值是：" + config.getId() + deliverName + ",当前方法=Calculate.showConfig()");
                    showCondition(config.getId(), deliverName);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            //添加删除按钮
            JButton deleteButton = new JButton("删除");
            deleteButton.addActionListener(e -> {
                try {
                    getDao().deleteDeliverConfig(config.getId());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                configFrame.setVisible(false);
                try {
                    showConfig(deliverName);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            //添加按钮到Panel
            jPanel.add(editButton);
            jPanel.add(deleteButton);
            //添加Panel到Frame
            configFrame.add(jPanel);
        }

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new FlowLayout());

        JButton newButton = new JButton("新增条件");
        newButton.addActionListener(e -> {
            try {
                showCondition(-1, deliverName);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        configPanel.add(newButton);

        JPanel savePanel = new JPanel();
        savePanel.setLayout(new FlowLayout());

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> {
            configFrame.setVisible(false);
        });

        savePanel.add(closeButton);
        configFrame.add(configPanel);
        configFrame.add(savePanel);
        configFrame.setVisible(true);

    }

    //条件编辑页面初始化
    private void showCondition(int configId, String deliverName) throws Exception {
        int deliverId = getDao().getDeliverId(deliverName);
        JFrame conditionFrame = new JFrame();
        conditionFrame.setSize(1024, 768);
        conditionFrame.setLayout(new GridLayout(4, 1));
        JLabel jLabel = new JLabel("条件设置", JLabel.CENTER);
        jLabel.setSize(100, 50);
        conditionFrame.add(jLabel);
        //条件设置
        JPanel conditionPanel = new JPanel();
        conditionPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        final DefaultComboBoxModel<String> conditions = new DefaultComboBoxModel<>();
        conditions.addElement("大于");
        conditions.addElement("小于");
        conditions.addElement("区间");
        final JComboBox<String> conditionCombo = new JComboBox<>(conditions);
        JScrollPane conditionListScrollPane = new JScrollPane(conditionCombo);
        conditionPanel.add(conditionListScrollPane);
        JTextField jTextField = new JTextField(5);
        JLabel label = new JLabel(",");
        JTextField jTextField1 = new JTextField(5);
        JLabel label1 = new JLabel("千克,每");
        JTextField jTextField2 = new JTextField(5);
        JLabel label2 = new JLabel("千克");
        JTextField jTextField3 = new JTextField(5);
        JLabel label3 = new JLabel("元,或总计");
        JTextField jTextField4 = new JTextField(5);
        JLabel label4 = new JLabel("元。");


        if (configId != -1) {
            Config config = getDao().getConfigById(configId);
            if (config.getMin() == 0) {
                conditionCombo.setSelectedIndex(1);
                jTextField1.setText(config.getMax() + "");
            } else if (config.getMax() == 0) {
                conditionCombo.setSelectedIndex(0);
                jTextField1.setText(config.getMin() + "");
            } else {
                conditionCombo.setSelectedIndex(2);
                jTextField.setText(config.getMin() + "");
                jTextField1.setText(config.getMax() + "");
                conditionPanel.add(jTextField);
            }
            jTextField2.setText(config.getUnit() + "");
            jTextField3.setText(config.getPrice() + "");
            jTextField4.setText(config.getTotalPrice() + "");
        }
        conditionPanel.add(jTextField1);
        conditionPanel.add(label1);
        conditionPanel.add(jTextField2);
        conditionPanel.add(label2);
        conditionPanel.add(jTextField3);
        conditionPanel.add(label3);
        conditionPanel.add(jTextField4);
        conditionPanel.add(label4);

        //地区设置
        JPanel areaPanel = new JPanel();
        areaPanel.setLayout(new GridLayout(5, 7));
        JCheckBox chkBeijing = new JCheckBox("北京市");
        JCheckBox chkTianjin = new JCheckBox("天津市");
        JCheckBox chkShanghai = new JCheckBox("上海市");
        JCheckBox chkChongqing = new JCheckBox("重庆市");
        JCheckBox chkHebei = new JCheckBox("河北省");
        JCheckBox chkShanxi = new JCheckBox("山西省");
        JCheckBox chkLiaoning = new JCheckBox("辽宁省");
        JCheckBox chkJilin = new JCheckBox("吉林省");
        JCheckBox chkHeilongjiang = new JCheckBox("黑龙江省");
        JCheckBox chkJiangsu = new JCheckBox("江苏省");
        JCheckBox chkZhejiang = new JCheckBox("浙江省");
        JCheckBox chkAnhui = new JCheckBox("安徽省");
        JCheckBox chkFujian = new JCheckBox("福建省");
        JCheckBox chkJiangxi = new JCheckBox("江西省");
        JCheckBox chkShandong = new JCheckBox("山东省");
        JCheckBox chkHenan = new JCheckBox("河南省");
        JCheckBox chkHubei = new JCheckBox("湖北省");
        JCheckBox chkHunan = new JCheckBox("湖南省");
        JCheckBox chkGuangdong = new JCheckBox("广东省");
        JCheckBox chkHainan = new JCheckBox("海南省");
        JCheckBox chkSichuan = new JCheckBox("四川省");
        JCheckBox chkGuizhou = new JCheckBox("贵州省");
        JCheckBox chkYunnan = new JCheckBox("云南省");
        JCheckBox chkShanXi = new JCheckBox("陕西省");
        JCheckBox chkGansu = new JCheckBox("甘肃省");
        JCheckBox chkQinghai = new JCheckBox("青海省");
        JCheckBox chkXizang = new JCheckBox("西藏自治区");
        JCheckBox chkGuangxi = new JCheckBox("广西壮族自治区");
        JCheckBox chkNeimenggu = new JCheckBox("内蒙古自治区");
        JCheckBox chkNingxia = new JCheckBox("宁夏回族自治区");
        JCheckBox chkXinjiang = new JCheckBox("新疆维吾尔自治区");
        JCheckBox chkXianggang = new JCheckBox("香港特别行政区");
        JCheckBox chkAomen = new JCheckBox("澳门特别行政区");
        JCheckBox chkTaiwan = new JCheckBox("台湾省");
        System.out.println("configId的值是：" + configId + ",当前方法=Calculate.showCondition()");
        List<String> provinceList = getDao().getDeliverArea(configId);
        System.out.println("provinceList的值是：" + provinceList + ",当前方法=Calculate.showCondition()");
        for (String s : provinceList)
            switch (s) {
                case "北京市":
                    chkBeijing.setSelected(true);
                    break;
                case "天津市":
                    chkTianjin.setSelected(true);
                    break;
                case "上海市":
                    chkShanghai.setSelected(true);
                    break;
                case "重庆市":
                    chkChongqing.setSelected(true);
                    break;
                case "河北省":
                    chkHebei.setSelected(true);
                    break;
                case "山西省":
                    chkShanxi.setSelected(true);
                    break;
                case "辽宁省":
                    chkLiaoning.setSelected(true);
                    break;
                case "吉林省":
                    chkJilin.setSelected(true);
                    break;
                case "黑龙江省":
                    chkHeilongjiang.setSelected(true);
                    break;
                case "江苏省":
                    chkJiangsu.setSelected(true);
                    break;
                case "浙江省":
                    chkZhejiang.setSelected(true);
                    break;
                case "安徽省":
                    chkAnhui.setSelected(true);
                    break;
                case "福建省":
                    chkFujian.setSelected(true);
                    break;
                case "江西省":
                    chkJiangxi.setSelected(true);
                    break;
                case "山东省":
                    chkShandong.setSelected(true);
                    break;
                case "河南省":
                    chkHenan.setSelected(true);
                    break;
                case "湖北省":
                    chkHubei.setSelected(true);
                    break;
                case "湖南省":
                    chkHunan.setSelected(true);
                    break;
                case "广东省":
                    chkGuangdong.setSelected(true);
                    break;
                case "海南省":
                    chkHainan.setSelected(true);
                    break;
                case "四川省":
                    chkSichuan.setSelected(true);
                    break;
                case "贵州省":
                    chkGuizhou.setSelected(true);
                    break;
                case "云南省":
                    chkYunnan.setSelected(true);
                    break;
                case "陕西省":
                    chkShanXi.setSelected(true);
                    break;
                case "甘肃省":
                    chkGansu.setSelected(true);
                    break;
                case "青海省":
                    chkQinghai.setSelected(true);
                    break;
                case "西藏自治区":
                    chkXizang.setSelected(true);
                    break;
                case "广西壮族自治区":
                    chkGuangxi.setSelected(true);
                    break;
                case "内蒙古自治区":
                    chkNeimenggu.setSelected(true);
                    break;
                case "宁夏回族自治区":
                    chkNingxia.setSelected(true);
                    break;
                case "新疆维吾尔自治区":
                    chkXinjiang.setSelected(true);
                    break;
                case "香港特别行政区":
                    chkXianggang.setSelected(true);
                    break;
                case "澳门特别行政区":
                    chkAomen.setSelected(true);
                    break;
                case "台湾省":
                    chkTaiwan.setSelected(true);
                    break;
            }

        areaPanel.add(chkBeijing);
        areaPanel.add(chkTianjin);
        areaPanel.add(chkShanghai);
        areaPanel.add(chkChongqing);
        areaPanel.add(chkHebei);
        areaPanel.add(chkShanxi);
        areaPanel.add(chkLiaoning);
        areaPanel.add(chkJilin);
        areaPanel.add(chkHeilongjiang);
        areaPanel.add(chkJiangsu);
        areaPanel.add(chkZhejiang);
        areaPanel.add(chkAnhui);
        areaPanel.add(chkFujian);
        areaPanel.add(chkJiangxi);
        areaPanel.add(chkShandong);
        areaPanel.add(chkHenan);
        areaPanel.add(chkHubei);
        areaPanel.add(chkHunan);
        areaPanel.add(chkGuangdong);
        areaPanel.add(chkHainan);
        areaPanel.add(chkSichuan);
        areaPanel.add(chkGuizhou);
        areaPanel.add(chkYunnan);
        areaPanel.add(chkShanXi);
        areaPanel.add(chkGansu);
        areaPanel.add(chkQinghai);
        areaPanel.add(chkXizang);
        areaPanel.add(chkGuangxi);
        areaPanel.add(chkNeimenggu);
        areaPanel.add(chkNingxia);
        areaPanel.add(chkXinjiang);
        areaPanel.add(chkXianggang);
        areaPanel.add(chkAomen);
        areaPanel.add(chkTaiwan);

        //添加下拉菜单点击监听
        conditionCombo.addActionListener(e -> {
            conditionPanel.removeAll();
            conditionPanel.add(conditionListScrollPane);
            if (conditionCombo.getSelectedIndex() == 2) {
                conditionPanel.add(jTextField);
                conditionPanel.add(label);
            }
            conditionPanel.add(jTextField1);
            conditionPanel.add(label1);
            conditionPanel.add(jTextField2);
            conditionPanel.add(label2);
            conditionPanel.add(jTextField3);
            conditionPanel.add(label3);
            conditionPanel.add(jTextField4);
            conditionPanel.add(label4);
            conditionPanel.validate();
            conditionPanel.repaint();
        });

        //保存按钮
        JPanel savePanel = new JPanel();
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener((ActionEvent e) -> {
            String error = null;
            String state = conditionCombo.getItemAt(conditionCombo.getSelectedIndex());
            double min = 0;
            double max = 0;
            double unit = 0;
            double price = 0;
            double totalPrice = 0;
            switch (state) {
                case "小于":
                    if (jTextField1.getText() != null && !"".equals(jTextField1.getText()))
                        max = Double.valueOf(jTextField1.getText());
                    else error = "输入重量区间不能为空！";

                    break;
                case "大于":
                    if (jTextField1.getText() != null && !"".equals(jTextField1.getText()))
                        min = Double.valueOf(jTextField1.getText());
                    else error = "输入重量区间不能为空";
                    break;
                case "区间":
                    if (jTextField.getText() != null && !"".equals(jTextField.getText()) && jTextField1.getText() != null && !"".equals(jTextField1.getText())) {
                        min = Double.valueOf(jTextField.getText());
                        max = Double.valueOf(jTextField1.getText());
                    } else error = "输入区间有误！";
                    break;
            }
            if (!"".equals(jTextField2.getText()) && Double.valueOf(jTextField2.getText()) != 0 && jTextField3.getText() != null
                    && !"".equals(jTextField3.getText()) && Double.valueOf(jTextField3.getText()) != 0) {
                unit = Double.valueOf(jTextField2.getText());
                price = Double.valueOf(jTextField3.getText());
            } else if (!"".equals(jTextField4.getText()) && Double.valueOf(jTextField4.getText()) != 0)
                totalPrice = Double.valueOf(jTextField4.getText());
            else error = "输入价格不能为空";
            Config config = new Config();
            config.setMin(min);
            config.setMax(max);
            config.setDeliverId(deliverId);
            config.setUnit(unit);
            config.setPrice(price);
            config.setTotalPrice(totalPrice);
            int newConfigId = 0;
            if (error != null) {
                int output = JOptionPane.showConfirmDialog(conditionFrame
                        , error
                        , "error"
                        , JOptionPane.DEFAULT_OPTION);
            } else {
                if (configId == -1) try {
                    getDao().insertConfig(config);
                    newConfigId = config.getId();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                else {
                    config.setId(configId);
                    try {
                        getDao().updateDeliverConfig(config);
                        getDao().deleteArea(configId);
                        newConfigId = configId;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                //保存地区信息
                List<String> selectedAreaList = new ArrayList<>();
                if (chkBeijing.isSelected()) selectedAreaList.add("北京市");
                if (chkTianjin.isSelected()) selectedAreaList.add("天津市");
                if (chkShanghai.isSelected()) selectedAreaList.add("上海市");
                if (chkChongqing.isSelected()) selectedAreaList.add("重庆市");
                if (chkHebei.isSelected()) selectedAreaList.add("河北省");
                if (chkShanxi.isSelected()) selectedAreaList.add("山西省");
                if (chkLiaoning.isSelected()) selectedAreaList.add("辽宁省");
                if (chkJilin.isSelected()) selectedAreaList.add("吉林省");
                if (chkHeilongjiang.isSelected()) selectedAreaList.add("黑龙江省");
                if (chkJiangsu.isSelected()) selectedAreaList.add("江苏省");
                if (chkZhejiang.isSelected()) selectedAreaList.add("浙江省");
                if (chkAnhui.isSelected()) selectedAreaList.add("安徽省");
                if (chkFujian.isSelected()) selectedAreaList.add("福建省");
                if (chkJiangxi.isSelected()) selectedAreaList.add("江西省");
                if (chkShandong.isSelected()) selectedAreaList.add("山东省");
                if (chkHenan.isSelected()) selectedAreaList.add("河南省");
                if (chkHubei.isSelected()) selectedAreaList.add("湖北省");
                if (chkHunan.isSelected()) selectedAreaList.add("湖南省");
                if (chkGuangdong.isSelected()) selectedAreaList.add("广东省");
                if (chkHainan.isSelected()) selectedAreaList.add("海南省");
                if (chkSichuan.isSelected()) selectedAreaList.add("四川省");
                if (chkGuizhou.isSelected()) selectedAreaList.add("贵州省");
                if (chkYunnan.isSelected()) selectedAreaList.add("云南省");
                if (chkShanXi.isSelected()) selectedAreaList.add("陕西省");
                if (chkGansu.isSelected()) selectedAreaList.add("甘肃省");
                if (chkQinghai.isSelected()) selectedAreaList.add("青海省");
                if (chkXizang.isSelected()) selectedAreaList.add("西藏自治区");
                if (chkGuangxi.isSelected()) selectedAreaList.add("广西壮族自治区");
                if (chkNeimenggu.isSelected()) selectedAreaList.add("内蒙古自治区");
                if (chkNingxia.isSelected()) selectedAreaList.add("宁夏回族自治区");
                if (chkXinjiang.isSelected()) selectedAreaList.add("新疆维吾尔自治区");
                if (chkXianggang.isSelected()) selectedAreaList.add("香港特别行政区");
                if (chkAomen.isSelected()) selectedAreaList.add("澳门特别行政区");
                if (chkTaiwan.isSelected()) selectedAreaList.add("台湾省");

                for (String area : selectedAreaList) {
                    try {
                        getDao().insertArea(newConfigId, area);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                conditionFrame.setVisible(false);
                try {
                    configFrame.setVisible(false);
                    showConfig(deliverName);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });
        savePanel.add(saveButton, Panel.BOTTOM_ALIGNMENT);

        conditionFrame.add(conditionPanel, Frame.CENTER_ALIGNMENT);
        conditionFrame.add(areaPanel);
        conditionFrame.add(savePanel);
        conditionFrame.setVisible(true);
    }

    public void copyFile(File fromFile, File toFile) throws IOException {
        FileInputStream ins = new FileInputStream(fromFile);
        FileOutputStream out = new FileOutputStream(toFile);
        byte[] b = new byte[1024];
        int n = 0;
        while ((n = ins.read(b)) != -1) {
            out.write(b, 0, n);
        }

        ins.close();
        out.close();
    }
}
