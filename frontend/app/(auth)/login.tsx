import React, { useState, useRef } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  Animated,
  ActivityIndicator,
  StatusBar,
  Dimensions,
} from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { authService } from "../../lib/services/authService";

const { width } = Dimensions.get("window");

const C = {
  void:  "#190019",
  deep:  "#2B124C",
  mid:   "#522B5B",
  dusk:  "#854F6C",
  blush: "#DFB6B2",
  petal: "#FBE4D8",
};

export default function LoginScreen() {
  const router = useRouter();
  const [phone, setPhone] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const cardScale    = useRef(new Animated.Value(0.92)).current;
  const cardOpacity  = useRef(new Animated.Value(0)).current;
  const titleY       = useRef(new Animated.Value(-28)).current;
  const titleOpacity = useRef(new Animated.Value(0)).current;
  const inputShake   = useRef(new Animated.Value(0)).current;

  React.useEffect(() => {
    Animated.parallel([
      Animated.spring(cardScale,   { toValue: 1, tension: 60, friction: 10, useNativeDriver: true }),
      Animated.timing(cardOpacity, { toValue: 1, duration: 520, useNativeDriver: true }),
      Animated.timing(titleOpacity,{ toValue: 1, duration: 600, delay: 140, useNativeDriver: true }),
      Animated.spring(titleY,      { toValue: 0, tension: 70, friction: 12, delay: 140, useNativeDriver: true }),
    ]).start();
  }, []);

  const shakeError = () => {
    Animated.sequence([
      Animated.timing(inputShake, { toValue: 10, duration: 60, useNativeDriver: true }),
      Animated.timing(inputShake, { toValue: -10, duration: 60, useNativeDriver: true }),
      Animated.timing(inputShake, { toValue: 8,  duration: 60, useNativeDriver: true }),
      Animated.timing(inputShake, { toValue: -8, duration: 60, useNativeDriver: true }),
      Animated.timing(inputShake, { toValue: 0,  duration: 60, useNativeDriver: true }),
    ]).start();
  };

  const handleSendOtp = async () => {
    setError("");
    const formatted = phone.startsWith("+91") ? phone : `+91${phone.replace(/^0/, "")}`;
    const phoneRegex = /^\+91[6-9]\d{9}$/;
    if (!phoneRegex.test(formatted)) {
      setError("Enter a valid 10-digit Indian mobile number");
      shakeError();
      return;
    }
    try {
      setLoading(true);
      await authService.sendOtp({ phone: formatted });
      router.push({ pathname: "/(auth)/verify", params: { phone: formatted } });
    } catch (err: any) {
      setError(err?.message ?? "Failed to send OTP. Try again.");
      shakeError();
    } finally {
      setLoading(false);
    }
  };

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
      <View style={[styles.blob, styles.blobCenter]} />

      <KeyboardAvoidingView
        style={styles.kav}
        behavior={Platform.OS === "ios" ? "padding" : "height"}
      >
        {/* ── Brand Block ── */}
        <Animated.View
          style={[
            styles.brandBlock,
            { opacity: titleOpacity, transform: [{ translateY: titleY }] },
          ]}
        >
          <View style={styles.brandMark}>
            <Text style={styles.brandMarkText}>◆</Text>
          </View>
          <Text style={styles.appName}>Spendly</Text>
          <Text style={styles.tagline}>Track every rupee, effortlessly.</Text>
        </Animated.View>

        {/* ── Card ── */}
        <Animated.View
          style={[
            styles.card,
            { opacity: cardOpacity, transform: [{ scale: cardScale }] },
          ]}
        >
          <LinearGradient
            colors={["rgba(251,228,216,0.08)", "rgba(251,228,216,0.02)"]}
            style={StyleSheet.absoluteFill}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 1 }}
          />

          <Text style={styles.cardTitle}>Sign in</Text>
          <Text style={styles.cardSub}>We'll send a one-time code to your number</Text>

          {/* Phone input */}
          <Animated.View
            style={[
              styles.inputWrapper,
              error ? styles.inputError : null,
              { transform: [{ translateX: inputShake }] },
            ]}
          >
            <View style={styles.prefixChip}>
              <Text style={styles.prefixFlag}>🇮🇳</Text>
              <Text style={styles.prefix}>+91</Text>
            </View>
            <View style={styles.divider} />
            <TextInput
              style={styles.input}
              value={phone.replace(/^\+91/, "")}
              onChangeText={(t) => {
                setError("");
                const digits = t.replace(/\D/g, "").slice(0, 10);
                setPhone(digits ? `+91${digits}` : "");
              }}
              placeholder="98765 43210"
              placeholderTextColor={C.dusk + "88"}
              keyboardType="phone-pad"
              maxLength={10}
              returnKeyType="send"
              onSubmitEditing={handleSendOtp}
              selectionColor={C.blush}
            />
          </Animated.View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          {/* CTA */}
          <TouchableOpacity
            onPress={handleSendOtp}
            disabled={loading}
            activeOpacity={0.82}
            style={styles.btnOuter}
          >
            <LinearGradient
              colors={[C.dusk, C.mid, C.deep]}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 1 }}
              style={styles.btn}
            >
              {loading ? (
                <ActivityIndicator color={C.petal} />
              ) : (
                <Text style={styles.btnText}>Send OTP →</Text>
              )}
            </LinearGradient>
          </TouchableOpacity>

          <View style={styles.cardBottomLine} />

          <Text style={styles.hint}>
            By continuing you agree to our{" "}
            <Text style={styles.hintLink}>Terms & Privacy</Text>
          </Text>
        </Animated.View>
      </KeyboardAvoidingView>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: C.void },
  kav:  { flex: 1, justifyContent: "center", paddingHorizontal: 24 },

  blob: { position: "absolute", borderRadius: 999, opacity: 0.18 },
  blobTL: {
    width: width * 0.7, height: width * 0.7,
    backgroundColor: C.mid,
    top: -width * 0.22, left: -width * 0.22,
  },
  blobBR: {
    width: width * 0.55, height: width * 0.55,
    backgroundColor: C.dusk,
    bottom: -width * 0.12, right: -width * 0.18,
  },
  blobCenter: {
    width: width * 0.35, height: width * 0.35,
    backgroundColor: C.dusk,
    opacity: 0.08,
    top: "35%", left: "30%",
  },

  // Brand
  brandBlock:    { alignItems: "center", marginBottom: 38 },
  brandMark: {
    width: 36, height: 36, borderRadius: 10,
    backgroundColor: "rgba(133,79,108,0.2)",
    borderWidth: 1, borderColor: "rgba(223,182,178,0.3)",
    justifyContent: "center", alignItems: "center",
    marginBottom: 14,
  },
  brandMarkText: { fontSize: 14, color: C.blush },
  appName: {
    fontFamily: Platform.OS === "ios" ? "Georgia" : "serif",
    fontSize: 44, fontWeight: "700", color: C.petal,
    letterSpacing: 2,
  },
  tagline: {
    marginTop: 8, fontSize: 13.5, color: C.blush,
    letterSpacing: 0.4, opacity: 0.8,
  },

  // Card
  card: {
    borderRadius: 24,
    borderWidth: 1,
    borderColor: "rgba(251,228,216,0.14)",
    padding: 26,
    overflow: "hidden",
    backgroundColor: "rgba(43,18,76,0.55)",
  },
  cardTitle: {
    fontSize: 24, fontWeight: "700", color: C.petal,
    fontFamily: Platform.OS === "ios" ? "Georgia" : "serif",
    marginBottom: 6,
  },
  cardSub: {
    fontSize: 13, color: C.blush, marginBottom: 26,
    opacity: 0.75, lineHeight: 20,
  },

  // Input
  inputWrapper: {
    flexDirection: "row", alignItems: "center",
    borderRadius: 13, borderWidth: 1.5,
    borderColor: "rgba(223,182,178,0.3)",
    backgroundColor: "rgba(25,0,25,0.45)",
    paddingHorizontal: 12, height: 54, marginBottom: 8,
  },
  inputError:  { borderColor: "#ff6b8a" },
  prefixChip:  { flexDirection: "row", alignItems: "center", gap: 5 },
  prefixFlag:  { fontSize: 16 },
  prefix:      { color: C.blush, fontSize: 15, fontWeight: "600", letterSpacing: 0.3 },
  divider:     { width: 1, height: 20, backgroundColor: "rgba(223,182,178,0.3)", marginHorizontal: 12 },
  input: {
    flex: 1, color: C.petal, fontSize: 17,
    letterSpacing: 1.2, fontWeight: "500",
  },
  errorText: { color: "#ff8fa3", fontSize: 12.5, marginBottom: 8, marginLeft: 2 },

  // Button
  btnOuter: {
    borderRadius: 13, marginTop: 10,
    shadowColor: C.dusk,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.5, shadowRadius: 16, elevation: 10,
  },
  btn:     { height: 54, borderRadius: 13, justifyContent: "center", alignItems: "center" },
  btnText: { color: C.petal, fontSize: 16, fontWeight: "700", letterSpacing: 0.8 },

  cardBottomLine: {
    height: 1, backgroundColor: C.blush,
    opacity: 0.18, marginTop: 22, marginBottom: 14,
  },
  hint:     { textAlign: "center", fontSize: 12, color: C.dusk, lineHeight: 18 },
  hintLink: { color: C.blush, textDecorationLine: "underline" },
});