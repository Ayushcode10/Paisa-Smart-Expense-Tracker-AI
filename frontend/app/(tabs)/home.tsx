import { View, Text, StyleSheet, StatusBar } from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import { Dimensions } from "react-native";

const { width } = Dimensions.get("window");

const C = {
  void: "#190019", deep: "#2B124C", mid: "#522B5B",
  dusk: "#854F6C", blush: "#DFB6B2", petal: "#FBE4D8",
};

export default function Home() {
  return (
    <View style={styles.root}>
      <StatusBar barStyle="light-content" />
      <LinearGradient
        colors={[C.void, C.deep, C.mid]}
        start={{ x: 0.15, y: 0 }}
        end={{ x: 0.85, y: 1 }}
        style={StyleSheet.absoluteFill}
      />
      <View style={[styles.blob, styles.blobTL]} />
      <View style={[styles.blob, styles.blobBR]} />

      <View style={styles.center}>
        <Text style={styles.label}>Home</Text>
        <Text style={styles.sub}>Your dashboard is coming soon</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: C.void },
  blob: { position: "absolute", borderRadius: 999, opacity: 0.18 },
  blobTL: {
    width: width * 0.7, height: width * 0.7,
    backgroundColor: C.mid, top: -width * 0.2, left: -width * 0.2,
  },
  blobBR: {
    width: width * 0.55, height: width * 0.55,
    backgroundColor: C.dusk, bottom: -width * 0.1, right: -width * 0.15,
  },
  center: { flex: 1, alignItems: "center", justifyContent: "center" },
  label: {
    fontSize: 32, fontWeight: "700", color: C.petal,
    letterSpacing: 1, marginBottom: 8,
  },
  sub: { fontSize: 14, color: C.blush, opacity: 0.7 },
});