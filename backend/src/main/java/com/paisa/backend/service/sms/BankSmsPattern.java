//package com.paisa.backend.service.sms;
//
//import lombok.Getter;
//import java.util.regex.Pattern;
//
//// Each enum value = one bank's SMS format
//// We store pre-compiled patterns (faster than compiling every time)
//@Getter
//public enum BankSmsPattern {
//
//    // ─────────────────────────────────────────────────────────
//    // HDFC BANK
//    // Sample: "Rs.1,299.00 debited from A/c XX4521 on 21-Jan-25.
//    //          Info: UPI/ZOMATO/ref. Avl Bal:Rs.12,450.00"
//    // ─────────────────────────────────────────────────────────
//    HDFC_DEBIT(
//            "HDFC",
//            "DEBIT",
//            Pattern.compile(
//                    "Rs\\.([\\d,]+\\.?\\d*)\\s+debited from A/c\\s+(\\w+).*?Info:\\s*(\\S+)",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    HDFC_CREDIT(
//            "HDFC",
//            "CREDIT",
//            Pattern.compile(
//                    "Rs\\.([\\d,]+\\.?\\d*)\\s+credited to A/c\\s+(\\w+).*?Info:\\s*(\\S+)",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    HDFC_UPI_SENT(
//            "HDFC",
//            "DEBIT",
//            Pattern.compile(
//                    "Sent Rs\\.(\\d+\\.?\\d*) From HDFC Bank A/C \\*(\\d+) To ([A-Za-z ]+)",
//                    Pattern.CASE_INSENSITIVE
//            )
//    ),
//
//    // ─────────────────────────────────────────────────────────
//    // SBI
//    // Sample: "Your A/c XX1234 is debited by Rs.500.00 on 21Jan25.
//    //          Txn done at AMAZON. Avl Bal: Rs.5,000.00"
//    // ─────────────────────────────────────────────────────────
//    SBI_DEBIT(
//            "SBI",
//            "DEBIT",
//            Pattern.compile(
//                    "Your A/c\\s+(\\w+)\\s+is debited by Rs\\.([\\d,]+\\.?\\d*).*?Txn done at\\s+([\\w\\s]+?)\\.",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    SBI_CREDIT(
//            "SBI",
//            "CREDIT",
//            Pattern.compile(
//                    "Your A/c\\s+(\\w+)\\s+is credited by Rs\\.([\\d,]+\\.?\\d*).*?from\\s+([\\w\\s]+?)\\.",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    // ─────────────────────────────────────────────────────────
//    // ICICI BANK
//    // Sample: "ICICI Bank Acct XX4321 debited with INR 1,500.00
//    //          on 21-Jan-2025; SWIGGY. Avail Bal INR 10,000.00"
//    // ─────────────────────────────────────────────────────────
//    ICICI_DEBIT(
//            "ICICI",
//            "DEBIT",
//            Pattern.compile(
//                    "ICICI Bank Acct\\s+(\\w+)\\s+debited with INR ([\\d,]+\\.?\\d*).*?;\\s*([\\w\\s]+?)\\.",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    ICICI_CREDIT(
//            "ICICI",
//            "CREDIT",
//            Pattern.compile(
//                    "ICICI Bank Acct\\s+(\\w+)\\s+credited with INR ([\\d,]+\\.?\\d*).*?from\\s+([\\w\\s]+?)\\.",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    // ─────────────────────────────────────────────────────────
//    // AXIS BANK
//    // Sample: "Rs.2000.00 debited from Axis Bank Acct XX5678
//    //          via UPI on 21-01-25. Merchant: FLIPKART"
//    // ─────────────────────────────────────────────────────────
//    AXIS_DEBIT(
//            "AXIS",
//            "DEBIT",
//            Pattern.compile(
//                    "Rs\\.([\\d,]+\\.?\\d*)\\s+debited from Axis Bank Acct\\s+(\\w+).*?Merchant:\\s*([\\w\\s]+)",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    // ─────────────────────────────────────────────────────────
//    // KOTAK BANK
//    // Sample: "Kotak Bank: Rs.3,000 debited from A/c XX9012
//    //          for UPI payment to BIGBASKET on 21/01/25"
//    // ─────────────────────────────────────────────────────────
//    KOTAK_DEBIT(
//            "KOTAK",
//            "DEBIT",
//            Pattern.compile(
//                    "Kotak Bank:.*?Rs\\.([\\d,]+\\.?\\d*)\\s+debited from A/c\\s+(\\w+).*?to\\s+([\\w\\s]+?)\\s+on",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    // ─────────────────────────────────────────────────────────
//    // GENERIC UPI (covers PhonePe, GPay, Paytm confirmations)
//    // Sample: "INR 499.00 debited from your A/c XX1234 for UPI
//    //          transfer to NETFLIX on 21-Jan-25"
//    // ─────────────────────────────────────────────────────────
//    GENERIC_UPI_DEBIT(
//            "GENERIC",
//            "DEBIT",
//            Pattern.compile(
//                    "(?:INR|Rs\\.?)\\s*([\\d,]+\\.?\\d*)\\s+debited.*?A/c\\s*(\\w+).*?" +
//                            "(?:to|at|for)\\s+([\\w\\s@]+?)(?:\\s+on|\\.|$)",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    ),
//
//    GENERIC_UPI_CREDIT(
//            "GENERIC",
//            "CREDIT",
//            Pattern.compile(
//                    "(?:INR|Rs\\.?)\\s*([\\d,]+\\.?\\d*)\\s+credited.*?A/c\\s*(\\w+).*?" +
//                            "(?:from|by)\\s+([\\w\\s@]+?)(?:\\s+on|\\.|$)",
//                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//            )
//    );
//
//    private final String bankName;
//    private final String transactionType;  // DEBIT or CREDIT
//    private final Pattern pattern;
//
//    BankSmsPattern(String bankName, String transactionType, Pattern pattern) {
//        this.bankName = bankName;
//        this.transactionType = transactionType;
//        this.pattern = pattern;
//    }
//}