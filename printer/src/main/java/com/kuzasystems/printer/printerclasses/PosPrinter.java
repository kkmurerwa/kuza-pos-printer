package com.kuzasystems.printer.printerclasses;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;

import com.morefun.yapi.device.printer.FontFamily;
import com.morefun.yapi.device.printer.MulPrintStrEntity;
import com.morefun.yapi.device.printer.MultipleAppPrinter;
import com.morefun.yapi.device.printer.OnPrintListener;
import com.morefun.yapi.device.printer.PrinterConfig;
import com.morefun.yapi.engine.DeviceServiceEngine;
import com.zcs.sdk.DriverManager;
import com.zcs.sdk.Printer;
import com.zcs.sdk.print.PrnStrFormat;
import com.zcs.sdk.print.PrnTextFont;
import com.zcs.sdk.print.PrnTextStyle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PosPrinter {
    private static DeviceServiceEngine deviceServiceEngine;
    private static MultipleAppPrinter printer;
    List<MulPrintStrEntity> list = new ArrayList<>();
    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private String printerModel;
    private List<PosPrinterEntry> entries;

    public List<PosPrinterEntry> getEntries() {
        return entries;
    }
    PosApiHelper posApiHelper ;

    private Printer mPrinter;
    public void setEntries(List<PosPrinterEntry> entries) {
        this.entries = entries;
    }

    public String getPrinterModel() {
        return printerModel;
    }

    public void setPrinterModel(String printerModel) {
        this.printerModel = printerModel;
    }

    public void initializePrinter(){
        setPrinterModel(getPrinterFromModelNo());
        switch (getPrinterModel().toUpperCase()){
            case "CS50":
            case "CS30":{
                posApiHelper = PosApiHelper.getInstance();
                posApiHelper.PrintInit();
                posApiHelper.PrintSetGray(5);
                printEntries();
                break;
            }case "M20":{
                DriverManager mDriverManager = DriverManager.getInstance();
                mPrinter = mDriverManager.getPrinter();
                printEntries();
                break;
            }case "MF919":{
                if (getDeviceService() == null) {
                    bindDeviceService(getContext());
                }else{
                    try {
                        printer = getDeviceService().getMultipleAppPrinter();
                        printEntries();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }default:break;

        }

    }
    public void printEntries(){
        for (PosPrinterEntry myEntry:this.getEntries()) {
            printEntry(myEntry);
        }

        printFooter();
        closePrinter();
    }

    @SuppressLint("RtlHardcoded")
    private void printEntry(PosPrinterEntry myEntry) {
            switch (getPrinterModel().toUpperCase()){
            case "CS50":
            case "CS30":{
                switch (myEntry.getAlignment()){
                    case "LEFT":{
                        posApiHelper.PrintSetAlign(0);
                        break;
                    }case "CENTER":{
                        posApiHelper.PrintSetAlign(1);
                        break;
                    }case "RIGHT":{
                        posApiHelper.PrintSetAlign(2);
                        break;
                    }
                }
                if (myEntry.isBold()){
                    posApiHelper.PrintSetFont((byte) 16, (byte) 16, (byte) 0x33);
                }else{
                    posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                }
                if (myEntry.getType().equals("LINE")){
                    posApiHelper.PrintSetFont((byte) 24, (byte) 24, (byte) 0x00);
                    posApiHelper.PrintStr("- - - - - - - - - - - - - - - -\n");
                }else if (myEntry.getType().equals("QR_CODE")){ //is qr code working.
                    posApiHelper.PrintBarcode(myEntry.getEntry(), 360, 360, "QR_CODE");
                }else {
                    posApiHelper.PrintStr(myEntry.getEntry());
                }
                break;
            }
            case "M20":{
                PrnStrFormat format = new PrnStrFormat();
                format.setFont(PrnTextFont.SERIF);
                switch (myEntry.getAlignment()){
                    case "LEFT":{
                        format.setAli(Layout.Alignment.ALIGN_NORMAL);
                        break;
                    }case "CENTER":{
                        format.setAli(Layout.Alignment.ALIGN_CENTER);
                        break;
                    }case "RIGHT":{
                        format.setAli(Layout.Alignment.ALIGN_OPPOSITE);
                        break;
                    }
                }
                if (myEntry.isBold()){
                    format.setTextSize(30);
                    format.setStyle(PrnTextStyle.BOLD);
                }else{
                    format.setTextSize(25);
                    format.setStyle(PrnTextStyle.NORMAL);
                }
                if (myEntry.getType().equals("LINE")){
                    mPrinter.setPrintAppendString("- - - - - - - - - - - - - - - - - - - - - - - - - - -", format);
                }else {
                    mPrinter.setPrintAppendString(myEntry.getEntry(), format);
                }
                break;
            }
            case "MF919":{
                int gravity =0;
                int fontSize;
                    switch (myEntry.getAlignment()){
                        case "LEFT":{
                            gravity = Gravity.LEFT;
                            break;
                        }case "CENTER":{
                            gravity = Gravity.CENTER;
                            break;
                        }case "RIGHT":{
                            gravity = Gravity.RIGHT;
                            break;
                        }
                    }
                    if (myEntry.isBold()){
                        fontSize =  FontFamily.BIG;
                    }else{
                        fontSize =  FontFamily.MIDDLE;
                    }
                    if (myEntry.getType().equals("LINE")){
                        list.add(new MulPrintStrEntity("- - - - - - - - - - - - - - - - - - - - - -",
                                FontFamily.BIG, false, Gravity.CENTER));
                    }else {
                        list.add(new MulPrintStrEntity(myEntry.getEntry(),
                                fontSize,false, gravity));
                    }

                break;
            }
            default:break;

        }
    }

    public void closePrinter(){
        switch (getPrinterModel().toUpperCase()){
            case "CS50":
            case "CS30":{
                for (int a=0;a<8;a++){
                    posApiHelper.PrintStr("\n");
                }
                posApiHelper.PrintStart();
                break;
            }case "M20":{
                PrnStrFormat format = new PrnStrFormat();
                format.setTextSize(30);
                format.setAli(Layout.Alignment.ALIGN_CENTER);
                format.setStyle(PrnTextStyle.BOLD);
                mPrinter.setPrintAppendString("\n", format);
                mPrinter.setPrintAppendString("\n", format);
                mPrinter.setPrintStart();
                break;
            }case "MF919":{
                Bundle config = new Bundle();
                config.putInt(PrinterConfig.COMMON_GRAYLEVEL, 200);
                list.add(new MulPrintStrEntity("\n", FontFamily.BIG));
                list.add(new MulPrintStrEntity("\n", FontFamily.BIG));
                try {
                    printer.printStr(list, new OnPrintListener.Stub() {
                        @Override
                        public void onPrintResult(int result) {

                        }
                    }, config);
                } catch (RemoteException ignored) {

                }
                break;
            }default:break;

        }

    }


    public void printFooter(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String printDate = formatter.format(date) + " " + formatterTime.format(date);
        String company = "Software by: www.kuzasystems.com";
        printEntry(new PosPrinterEntry("Printed On: " + printDate, false, "CENTER","LINE")) ;
        printEntry(new PosPrinterEntry("Printed On: " + printDate, false, "CENTER","STRING")) ;
        printEntry(new PosPrinterEntry(company, false, "CENTER","STRING")) ;

    }
    public void print(Context context,List<PosPrinterEntry> entries){
        setContext(context);
        setEntries(entries);
        initializePrinter();
    }
    public static class PosPrinterEntry {
        private final String entry;
        private final boolean bold;

        private final String alignment;// (left,center,right)
        private final String type;// STRING,LINE,IMAGE,QRCODE


        public PosPrinterEntry(String entry, boolean bold, String alignment,String type) {
            this.entry = entry;
            this.bold = bold;
            this.alignment = alignment;
            this.type = type;
        }

        public String getEntry() {
            return entry;
        }


        public boolean isBold() {
            return bold;
        }

        public String getAlignment() {
            return alignment;
        }

        public String getType() {
            return type;
        }
    }
    public static String getPrinterFromModelNo(){
        try{
            String model = Build.MODEL.trim();
            return  model.toLowerCase();
        }catch (Exception ignored){
        }
        return "";
    }
    public static DeviceServiceEngine getDeviceService() {
        return deviceServiceEngine;
    }
    public void bindDeviceService(Context context) {
        if (null != deviceServiceEngine) {
            return;
        }

        final ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                deviceServiceEngine = null;
                Log.e("Transaction Fragment", "======onServiceDisconnected======");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                deviceServiceEngine = DeviceServiceEngine.Stub.asInterface(service);
                try {
                    printer = getDeviceService().getMultipleAppPrinter();
                    printEntries();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                linkToDeath(service);
            }

            private void linkToDeath(IBinder service) {
                try {
                    service.linkToDeath(() -> {
                        deviceServiceEngine = null;
                        bindDeviceService(context);
                    }, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        Intent intent = new Intent();
        String SERVICE_ACTION = "com.morefun.ysdk.service";
        intent.setAction(SERVICE_ACTION);
        String SERVICE_PACKAGE = "com.morefun.ysdk";
        intent.setPackage(SERVICE_PACKAGE);

        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
}
