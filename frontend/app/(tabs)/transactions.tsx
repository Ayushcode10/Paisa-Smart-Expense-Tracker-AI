//transactions.tsx

import React, { useState, useEffect, useCallback, useRef } from "react";
import {
  View, Text, StyleSheet, FlatList, TouchableOpacity,
  TextInput, StatusBar, Dimensions, ActivityIndicator,
  Modal, ScrollView, RefreshControl, Platform,
  KeyboardAvoidingView,
} from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import {
  transactionService,
  TransactionResponse,
  TransactionSummaryResponse,
  TransactionType,
  TransactionSource,
  GetTransactionsParams,
} from "../../lib/services/transactionService";

const { width } = Dimensions.get("window");

const C = {
  void:  "#190019",
  deep:  "#2B124C",
  mid:   "#522B5B",
  dusk:  "#854F6C",
  blush: "#DFB6B2",
  petal: "#FBE4D8",
  green: "#4CAF7D",
  red:   "#E05C6B",
};

// ── Source icon map ───────────────────────────────────────────
const SOURCE_ICONS: Record<string, string> = {
  UPI: "⚡", CARD: "💳", CASH: "💵", WALLET: "👛",
  NET_BANKING: "🏦", IMPS: "🔄", NEFT: "🔄", RTGS: "🔄",
  ATM: "🏧", EMI: "📅", UNKNOWN: "•",
};

const CATEGORY_ICONS: Record<string, string> = {
  Food: "🍔", Income: "💰", Entertainment: "🎬", Bills: "📄",
  Groceries: "🛒", Travel: "✈️", Shopping: "🛍", Medical: "💊",
  Fuel: "⛽", Education: "📚", Uncategorized: "•",
};

const TYPE_FILTERS: { label: string; value: TransactionType | "ALL" }[] = [
  { label: "All", value: "ALL" },
  { label: "Debit", value: "DEBIT" },
  { label: "Credit", value: "CREDIT" },
];

// ── Formatters ────────────────────────────────────────────────
function formatAmount(n: number) {
  if (n >= 100000) return `₹${(n / 100000).toFixed(1)}L`;
  if (n >= 1000)   return `₹${(n / 1000).toFixed(1)}K`;
  return `₹${n.toLocaleString("en-IN")}`;
}

function formatDate(iso: string) {
  const d = new Date(iso);
  const now = new Date();
  const diff = Math.floor((now.getTime() - d.getTime()) / 86400000);
  if (diff === 0) return "Today";
  if (diff === 1) return "Yesterday";
  return d.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString("en-IN", {
    hour: "2-digit", minute: "2-digit", hour12: true,
  });
}

// ── Add Transaction Modal ─────────────────────────────────────
function AddTransactionModal({
  visible,
  onClose,
  onAdded,
}: {
  visible: boolean;
  onClose: () => void;
  onAdded: () => void;
}) {
  const [amount, setAmount]     = useState("");
  const [merchant, setMerchant] = useState("");
  const [type, setType]         = useState<TransactionType>("DEBIT");
  const [category, setCategory] = useState("");
  const [source, setSource]     = useState<TransactionSource>("UPI");
  const [note, setNote]         = useState("");
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState("");

  const reset = () => {
    setAmount(""); setMerchant(""); setType("DEBIT");
    setCategory(""); setSource("UPI"); setNote(""); setError("");
  };

  const handleAdd = async () => {
    if (!amount || isNaN(Number(amount)) || Number(amount) <= 0) {
      setError("Enter a valid amount"); return;
    }
    if (!merchant.trim()) { setError("Enter merchant name"); return; }
    try {
      setLoading(true);
      await transactionService.create({
        amount: Number(amount),
        merchant: merchant.trim(),
        type,
        category: category.trim() || undefined,
        source,
        note: note.trim() || undefined,
        transactionDate: new Date().toISOString(),
      });
      reset();
      onAdded();
      onClose();
    } catch (e: any) {
      console.log("CREATE ERROR:", e?.message);
      setError(e?.message ?? "Failed to add. Try again.");
    } finally {
      setLoading(false);
    }
  };

  const sources: TransactionSource[] = ["UPI", "CARD", "CASH", "WALLET", "NET_BANKING", "EMI"];

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={modal.overlay}>
        <KeyboardAvoidingView
          behavior={Platform.OS === "ios" ? "padding" : "height"}
          style={{ width: "100%" }}
        >
          <View style={modal.sheet}>
            <LinearGradient
              colors={[C.deep, C.mid]}
              style={StyleSheet.absoluteFill}
            />
            <View style={modal.handle} />

            <View style={modal.header}>
              <Text style={modal.title}>Add Transaction</Text>
              <TouchableOpacity onPress={() => { reset(); onClose(); }}>
                <Text style={modal.closeBtn}>✕</Text>
              </TouchableOpacity>
            </View>

            <ScrollView showsVerticalScrollIndicator={false} keyboardShouldPersistTaps="handled">
              {/* Type toggle */}
              <View style={modal.typeRow}>
                {(["DEBIT", "CREDIT"] as TransactionType[]).map((t) => (
                  <TouchableOpacity
                    key={t}
                    style={[modal.typeBtn, type === t && (t === "DEBIT" ? modal.typeBtnDebit : modal.typeBtnCredit)]}
                    onPress={() => setType(t)}
                  >
                    <Text style={[modal.typeBtnText, type === t && modal.typeBtnTextActive]}>
                      {t === "DEBIT" ? "↑ Debit" : "↓ Credit"}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>

              {/* Amount */}
              <Text style={modal.label}>Amount</Text>
              <View style={modal.inputRow}>
                <Text style={modal.rupee}>₹</Text>
                <TextInput
                  style={modal.input}
                  value={amount}
                  onChangeText={t => { setError(""); setAmount(t.replace(/[^0-9.]/g, "")); }}
                  placeholder="0.00"
                  placeholderTextColor={C.dusk + "88"}
                  keyboardType="decimal-pad"
                  selectionColor={C.blush}
                />
              </View>

              {/* Merchant */}
              <Text style={modal.label}>Merchant / Description</Text>
              <TextInput
                style={modal.inputBox}
                value={merchant}
                onChangeText={t => { setError(""); setMerchant(t); }}
                placeholder="e.g. Zomato, Amazon"
                placeholderTextColor={C.dusk + "88"}
                selectionColor={C.blush}
              />

              {/* Category */}
              <Text style={modal.label}>Category (optional — AI will assign)</Text>
              <TextInput
                style={modal.inputBox}
                value={category}
                onChangeText={setCategory}
                placeholder="e.g. Food, Travel"
                placeholderTextColor={C.dusk + "88"}
                selectionColor={C.blush}
              />

              {/* Source */}
              <Text style={modal.label}>Source</Text>
              <ScrollView horizontal showsHorizontalScrollIndicator={false} style={modal.chipScroll}>
                {sources.map((s) => (
                  <TouchableOpacity
                    key={s}
                    style={[modal.chip, source === s && modal.chipActive]}
                    onPress={() => setSource(s)}
                  >
                    <Text style={[modal.chipText, source === s && modal.chipTextActive]}>
                      {SOURCE_ICONS[s]} {s}
                    </Text>
                  </TouchableOpacity>
                ))}
              </ScrollView>

              {/* Note */}
              <Text style={modal.label}>Note (optional)</Text>
              <TextInput
                style={modal.inputBox}
                value={note}
                onChangeText={setNote}
                placeholder="e.g. Lunch with team"
                placeholderTextColor={C.dusk + "88"}
                selectionColor={C.blush}
              />

              {error ? <Text style={modal.error}>{error}</Text> : null}

              <TouchableOpacity
                style={modal.addBtn}
                onPress={handleAdd}
                disabled={loading}
                activeOpacity={0.82}
              >
                <LinearGradient
                  colors={[C.dusk, C.mid]}
                  start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }}
                  style={modal.addBtnGrad}
                >
                  {loading
                    ? <ActivityIndicator color={C.petal} />
                    : <Text style={modal.addBtnText}>Add Transaction →</Text>
                  }
                </LinearGradient>
              </TouchableOpacity>

              <View style={{ height: 32 }} />
            </ScrollView>
          </View>
        </KeyboardAvoidingView>
      </View>
    </Modal>
  );
}

// ── Transaction Card ──────────────────────────────────────────
function TxnCard({ item }: { item: TransactionResponse }) {
  const isDebit = item.type === "DEBIT";
  const icon    = CATEGORY_ICONS[item.category] ?? "•";
  const lowConf = (item.categoryConfidence ?? 1) < 0.6;

  return (
    <View style={card.root}>
      <View style={card.iconBubble}>
        <Text style={card.iconText}>{icon}</Text>
      </View>
      <View style={card.mid}>
        <View style={card.topRow}>
          <Text style={card.merchant} numberOfLines={1}>{item.merchant}</Text>
          <Text style={[card.amount, isDebit ? card.amountDebit : card.amountCredit]}>
            {isDebit ? "−" : "+"}{formatAmount(item.amount)}
          </Text>
        </View>
        <View style={card.bottomRow}>
          <Text style={card.category}>
            {lowConf ? "⚠️ " : ""}{item.category}
          </Text>
          <Text style={card.dot}>·</Text>
          <Text style={card.source}>{SOURCE_ICONS[item.source] ?? "•"} {item.source}</Text>
          <Text style={card.dot}>·</Text>
          <Text style={card.date}>{formatDate(item.transactionDate)}</Text>
          <Text style={card.time}>, {formatTime(item.transactionDate)}</Text>
        </View>
        {item.note ? <Text style={card.note} numberOfLines={1}>"{item.note}"</Text> : null}
      </View>
    </View>
  );
}

// ── Summary Bar ───────────────────────────────────────────────
function SummaryBar({ summary }: { summary: TransactionSummaryResponse }) {
  return (
    <View style={sumStyle.row}>
      <View style={sumStyle.item}>
        <Text style={sumStyle.label}>Spent</Text>
        <Text style={[sumStyle.value, { color: C.red }]}>
          {formatAmount(summary.totalDebit)}
        </Text>
      </View>
      <View style={sumStyle.divider} />
      <View style={sumStyle.item}>
        <Text style={sumStyle.label}>Received</Text>
        <Text style={[sumStyle.value, { color: C.green }]}>
          {formatAmount(summary.totalCredit)}
        </Text>
      </View>
      <View style={sumStyle.divider} />
      <View style={sumStyle.item}>
        <Text style={sumStyle.label}>Net</Text>
        <Text style={[sumStyle.value, { color: C.blush }]}>
          {formatAmount(summary.netBalance)}
        </Text>
      </View>
    </View>
  );
}

// ── Main Screen ───────────────────────────────────────────────
export default function Transactions() {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [summary, setSummary]           = useState<TransactionSummaryResponse | null>(null);
  const [loading, setLoading]           = useState(true);
  const [refreshing, setRefreshing]     = useState(false);
  const [loadingMore, setLoadingMore]   = useState(false);
  const [page, setPage]                 = useState(0);
  const [hasMore, setHasMore]           = useState(true);
  const [error, setError]               = useState<string | null>(null);

  const [search, setSearch]         = useState("");
  const [typeFilter, setTypeFilter] = useState<TransactionType | "ALL">("ALL");
  const [showAdd, setShowAdd]       = useState(false);

  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const monthFrom = () => {
    const d = new Date(); d.setDate(1); d.setHours(0, 0, 0, 0);
    return d.toISOString();
  };
  const monthTo = () => {
    const d = new Date();
    d.setMonth(d.getMonth() + 1); d.setDate(0); d.setHours(23, 59, 59, 999);
    return d.toISOString();
  };

  const buildParams = (pg: number): GetTransactionsParams => ({
    page: pg,
    size: 20,
    search: search || undefined,
    type: typeFilter === "ALL" ? undefined : typeFilter,
  });

  const fetchData = useCallback(async (reset = false) => {
    setError(null);
    try {
      const pg = reset ? 0 : page;
      const [listRes, sumRes] = await Promise.all([
        transactionService.getAll(buildParams(pg)),
        reset ? transactionService.getSummary(monthFrom(), monthTo()) : Promise.resolve(summary),
      ]);

      setTransactions(reset ? listRes.content : (prev) => [...prev, ...listRes.content]);
      if (reset && sumRes) setSummary(sumRes as TransactionSummaryResponse);
      // Spring Page uses "last" and "number" at top level (not nested under "pagination")
      setHasMore(!listRes.last);
      setPage(listRes.number + 1);
    } catch (e: any) {
      setError(e?.message ?? "Failed to load transactions");
    } finally {
      setLoading(false);
      setRefreshing(false);
      setLoadingMore(false);
    }
  }, [page, search, typeFilter, summary]);

  useEffect(() => {
    setLoading(true);
    setPage(0);
    setTransactions([]);
    fetchData(true);
  }, [typeFilter]);

  // Debounced search
  useEffect(() => {
    if (searchTimer.current) clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => {
      setLoading(true); setPage(0); setTransactions([]);
      fetchData(true);
    }, 400);
    return () => { if (searchTimer.current) clearTimeout(searchTimer.current); };
  }, [search]);

  const onRefresh = () => {
    setRefreshing(true); setPage(0); setTransactions([]);
    fetchData(true);
  };

  const onEndReached = () => {
    if (hasMore && !loadingMore) { setLoadingMore(true); fetchData(); }
  };

  const renderContent = () => {
    if (loading) {
      return (
        <View style={styles.center}>
          <ActivityIndicator color={C.blush} size="large" />
        </View>
      );
    }

    if (error) {
      return (
        <View style={styles.center}>
          <Text style={styles.emptyIcon}>⚠️</Text>
          <Text style={styles.emptyText}>Could not load transactions</Text>
          <Text style={styles.emptySub}>{error}</Text>
          <TouchableOpacity
            onPress={() => { setLoading(true); fetchData(true); }}
            style={styles.retryBtn}
            activeOpacity={0.75}
          >
            <Text style={styles.retryText}>Retry</Text>
          </TouchableOpacity>
        </View>
      );
    }

    return (
      <FlatList
        data={transactions}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => <TxnCard item={item} />}
        contentContainerStyle={styles.listContent}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={C.blush}
          />
        }
        onEndReached={onEndReached}
        onEndReachedThreshold={0.3}
        ListFooterComponent={
          loadingMore
            ? <ActivityIndicator color={C.blush} style={{ marginVertical: 16 }} />
            : null
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyIcon}>💸</Text>
            <Text style={styles.emptyText}>No transactions found</Text>
            <Text style={styles.emptySub}>Add one or adjust your filters</Text>
          </View>
        }
      />
    );
  };

  return (
    <View style={styles.root}>
      <StatusBar barStyle="light-content" />
      <LinearGradient
        colors={[C.void, C.deep, C.mid]}
        start={{ x: 0.15, y: 0 }} end={{ x: 0.85, y: 1 }}
        style={StyleSheet.absoluteFill}
      />
      <View style={[styles.blob, styles.blobTL]} />
      <View style={[styles.blob, styles.blobBR]} />

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.pageTitle}>Transactions</Text>
        <TouchableOpacity style={styles.addBtn} onPress={() => setShowAdd(true)}>
          <LinearGradient
            colors={[C.dusk, C.mid]}
            start={{ x: 0, y: 0 }} end={{ x: 1, y: 1 }}
            style={styles.addBtnGrad}
          >
            <Text style={styles.addBtnText}>＋ Add</Text>
          </LinearGradient>
        </TouchableOpacity>
      </View>

      {/* Summary */}
      {summary && <SummaryBar summary={summary} />}

      {/* Search */}
      <View style={styles.searchRow}>
        <View style={styles.searchBox}>
          <Text style={styles.searchIcon}>🔍</Text>
          <TextInput
            style={styles.searchInput}
            value={search}
            onChangeText={setSearch}
            placeholder="Search merchants…"
            placeholderTextColor={C.dusk + "88"}
            selectionColor={C.blush}
            returnKeyType="search"
          />
          {search.length > 0 && (
            <TouchableOpacity onPress={() => setSearch("")}>
              <Text style={styles.clearBtn}>✕</Text>
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Type filter chips */}
      <View style={styles.filterRow}>
        {TYPE_FILTERS.map((f) => (
          <TouchableOpacity
            key={f.value}
            style={[styles.filterChip, typeFilter === f.value && styles.filterChipActive]}
            onPress={() => setTypeFilter(f.value)}
          >
            <Text style={[styles.filterChipText, typeFilter === f.value && styles.filterChipTextActive]}>
              {f.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Content */}
      {renderContent()}

      {/* Add Modal */}
      <AddTransactionModal
        visible={showAdd}
        onClose={() => setShowAdd(false)}
        onAdded={() => { setPage(0); setTransactions([]); fetchData(true); }}
      />
    </View>
  );
}

// ── Styles ────────────────────────────────────────────────────
const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: C.void },
  blob: { position: "absolute", borderRadius: 999 },
  blobTL: {
    width: width * 0.7, height: width * 0.7, opacity: 0.12,
    backgroundColor: C.mid, top: -width * 0.2, left: -width * 0.2,
  },
  blobBR: {
    width: width * 0.55, height: width * 0.55, opacity: 0.12,
    backgroundColor: C.dusk, bottom: -width * 0.1, right: -width * 0.15,
  },

  header: {
    flexDirection: "row", justifyContent: "space-between", alignItems: "center",
    paddingHorizontal: 20, paddingTop: Platform.OS === "ios" ? 60 : 44, paddingBottom: 8,
  },
  pageTitle: {
    fontSize: 26, fontWeight: "700", color: C.petal,
    fontFamily: Platform.OS === "ios" ? "Georgia" : "serif",
  },
  addBtn: {
    borderRadius: 20,
    shadowColor: C.dusk, shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.5, shadowRadius: 10, elevation: 8,
  },
  addBtnGrad: { paddingHorizontal: 16, paddingVertical: 9, borderRadius: 20 },
  addBtnText: { color: C.petal, fontWeight: "700", fontSize: 13.5 },

  searchRow: { paddingHorizontal: 16, marginTop: 12, marginBottom: 4 },
  searchBox: {
    flexDirection: "row", alignItems: "center",
    backgroundColor: "rgba(43,18,76,0.7)",
    borderRadius: 14, borderWidth: 1,
    borderColor: "rgba(223,182,178,0.2)",
    paddingHorizontal: 14, height: 46,
  },
  searchIcon: { fontSize: 14, marginRight: 8 },
  searchInput: { flex: 1, color: C.petal, fontSize: 15 },
  clearBtn: { color: C.dusk, fontSize: 14, paddingLeft: 8 },

  filterRow: {
    flexDirection: "row", paddingHorizontal: 16,
    gap: 8, marginVertical: 10,
  },
  filterChip: {
    paddingHorizontal: 16, paddingVertical: 7, borderRadius: 20,
    borderWidth: 1, borderColor: "rgba(133,79,108,0.4)",
    backgroundColor: "rgba(43,18,76,0.5)",
  },
  filterChipActive: {
    backgroundColor: C.dusk,
    borderColor: C.blush,
  },
  filterChipText:       { color: C.dusk, fontSize: 13, fontWeight: "600" },
  filterChipTextActive: { color: C.petal },

  listContent: { paddingHorizontal: 16, paddingBottom: 120 },
  center: { flex: 1, justifyContent: "center", alignItems: "center" },

  empty: { alignItems: "center", paddingTop: 60 },
  emptyIcon: { fontSize: 40, marginBottom: 12 },
  emptyText: { color: C.blush, fontSize: 16, fontWeight: "600", marginBottom: 4 },
  emptySub:  { color: C.dusk, fontSize: 13 },

  retryBtn: {
    marginTop: 20, paddingHorizontal: 28, paddingVertical: 10,
    borderRadius: 20, borderWidth: 1, borderColor: C.blush,
    backgroundColor: "rgba(223,182,178,0.1)",
  },
  retryText: { color: C.blush, fontWeight: "600", fontSize: 14 },
});

// ── Card styles ───────────────────────────────────────────────
const card = StyleSheet.create({
  root: {
    flexDirection: "row", alignItems: "flex-start",
    backgroundColor: "rgba(43,18,76,0.6)",
    borderRadius: 16, borderWidth: 1,
    borderColor: "rgba(251,228,216,0.1)",
    padding: 14, marginBottom: 10,
  },
  iconBubble: {
    width: 40, height: 40, borderRadius: 12,
    backgroundColor: "rgba(133,79,108,0.25)",
    borderWidth: 1, borderColor: "rgba(223,182,178,0.2)",
    justifyContent: "center", alignItems: "center",
    marginRight: 12,
  },
  iconText: { fontSize: 18 },
  mid: { flex: 1 },
  topRow: { flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start" },
  merchant: { color: C.petal, fontSize: 15, fontWeight: "600", flex: 1, marginRight: 8 },
  amount: { fontSize: 16, fontWeight: "700" },
  amountDebit:  { color: C.red },
  amountCredit: { color: C.green },
  bottomRow: { flexDirection: "row", alignItems: "center", marginTop: 4, flexWrap: "wrap" },
  category: { color: C.blush, fontSize: 11.5 },
  dot: { color: C.dusk, marginHorizontal: 4, fontSize: 11 },
  source: { color: C.dusk, fontSize: 11.5 },
  date: { color: C.dusk, fontSize: 11.5 },
  time: { color: C.dusk + "99", fontSize: 10.5 },
  note: { color: C.dusk, fontSize: 11, marginTop: 4, fontStyle: "italic" },
});

// ── Summary styles ────────────────────────────────────────────
const sumStyle = StyleSheet.create({
  row: {
    flexDirection: "row",
    marginHorizontal: 16, marginTop: 4,
    backgroundColor: "rgba(43,18,76,0.7)",
    borderRadius: 16, borderWidth: 1,
    borderColor: "rgba(251,228,216,0.1)",
    paddingVertical: 14, paddingHorizontal: 8,
  },
  item: { flex: 1, alignItems: "center" },
  label: { color: C.dusk, fontSize: 11, marginBottom: 4, fontWeight: "500" },
  value: { fontSize: 16, fontWeight: "700" },
  divider: { width: 1, backgroundColor: "rgba(223,182,178,0.15)", marginVertical: 4 },
});

// ── Modal styles ──────────────────────────────────────────────
const modal = StyleSheet.create({
  overlay: {
    flex: 1, justifyContent: "flex-end",
    backgroundColor: "rgba(10,0,20,0.6)",
  },
  sheet: {
    borderTopLeftRadius: 28, borderTopRightRadius: 28,
    overflow: "hidden", padding: 20,
    borderWidth: 1, borderColor: "rgba(251,228,216,0.12)",
    minHeight: 500,
  },
  handle: {
    width: 40, height: 4, borderRadius: 2,
    backgroundColor: "rgba(223,182,178,0.4)",
    alignSelf: "center", marginBottom: 16,
  },
  header: {
    flexDirection: "row", justifyContent: "space-between",
    alignItems: "center", marginBottom: 20,
  },
  title: {
    fontSize: 20, fontWeight: "700", color: C.petal,
    fontFamily: Platform.OS === "ios" ? "Georgia" : "serif",
  },
  closeBtn: { color: C.dusk, fontSize: 18, padding: 4 },

  typeRow: { flexDirection: "row", gap: 10, marginBottom: 20 },
  typeBtn: {
    flex: 1, paddingVertical: 11, borderRadius: 12,
    borderWidth: 1.5, borderColor: "rgba(133,79,108,0.4)",
    alignItems: "center",
    backgroundColor: "rgba(25,0,25,0.4)",
  },
  typeBtnDebit:  { backgroundColor: "rgba(224,92,107,0.2)", borderColor: C.red },
  typeBtnCredit: { backgroundColor: "rgba(76,175,125,0.2)", borderColor: C.green },
  typeBtnText:       { color: C.dusk, fontWeight: "600", fontSize: 14 },
  typeBtnTextActive: { color: C.petal },

  label: { color: C.blush, fontSize: 12.5, marginBottom: 7, marginTop: 14, opacity: 0.85 },

  inputRow: {
    flexDirection: "row", alignItems: "center",
    backgroundColor: "rgba(25,0,25,0.5)",
    borderRadius: 12, borderWidth: 1.5,
    borderColor: "rgba(223,182,178,0.3)",
    paddingHorizontal: 14, height: 52,
  },
  rupee: { color: C.blush, fontSize: 20, marginRight: 8, fontWeight: "700" },
  input: { flex: 1, color: C.petal, fontSize: 22, fontWeight: "700" },

  inputBox: {
    backgroundColor: "rgba(25,0,25,0.5)",
    borderRadius: 12, borderWidth: 1.5,
    borderColor: "rgba(223,182,178,0.3)",
    paddingHorizontal: 14, height: 48,
    color: C.petal, fontSize: 15,
  },

  chipScroll: { marginTop: 4 },
  chip: {
    paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
    borderWidth: 1, borderColor: "rgba(133,79,108,0.4)",
    backgroundColor: "rgba(25,0,25,0.4)",
    marginRight: 8,
  },
  chipActive: { backgroundColor: C.dusk, borderColor: C.blush },
  chipText:       { color: C.dusk, fontSize: 12.5, fontWeight: "600" },
  chipTextActive: { color: C.petal },

  error: { color: "#ff8fa3", fontSize: 12.5, marginTop: 10, textAlign: "center" },

  addBtn: {
    borderRadius: 14, marginTop: 24,
    shadowColor: C.dusk, shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.5, shadowRadius: 16, elevation: 10,
  },
  addBtnGrad: { height: 54, borderRadius: 14, justifyContent: "center", alignItems: "center" },
  addBtnText: { color: C.petal, fontSize: 16, fontWeight: "700", letterSpacing: 0.5 },
});