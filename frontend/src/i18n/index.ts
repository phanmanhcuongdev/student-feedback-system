import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { initReactI18next } from "react-i18next";
import enAdmin from "./locales/en/admin.json";
import enAccount from "./locales/en/account.json";
import enAuth from "./locales/en/auth.json";
import enCommon from "./locales/en/common.json";
import enFeedback from "./locales/en/feedback.json";
import enLayout from "./locales/en/layout.json";
import enNotifications from "./locales/en/notifications.json";
import enSurveys from "./locales/en/surveys.json";
import enSurveyResults from "./locales/en/surveyResults.json";
import enValidation from "./locales/en/validation.json";
import viAdmin from "./locales/vi/admin.json";
import viAccount from "./locales/vi/account.json";
import viAuth from "./locales/vi/auth.json";
import viCommon from "./locales/vi/common.json";
import viFeedback from "./locales/vi/feedback.json";
import viLayout from "./locales/vi/layout.json";
import viNotifications from "./locales/vi/notifications.json";
import viSurveys from "./locales/vi/surveys.json";
import viSurveyResults from "./locales/vi/surveyResults.json";
import viValidation from "./locales/vi/validation.json";

const LANGUAGE_STORAGE_KEY = "i18nextLng";

function getInitialLanguage() {
    if (typeof window === "undefined") {
        return "vi";
    }

    try {
        return window.localStorage.getItem(LANGUAGE_STORAGE_KEY) || "vi";
    } catch {
        return "vi";
    }
}

void i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources: {
            vi: {
                account: viAccount,
                admin: viAdmin,
                auth: viAuth,
                common: viCommon,
                feedback: viFeedback,
                layout: viLayout,
                notifications: viNotifications,
                surveys: viSurveys,
                surveyResults: viSurveyResults,
                validation: viValidation,
            },
            en: {
                account: enAccount,
                admin: enAdmin,
                auth: enAuth,
                common: enCommon,
                feedback: enFeedback,
                layout: enLayout,
                notifications: enNotifications,
                surveys: enSurveys,
                surveyResults: enSurveyResults,
                validation: enValidation,
            },
        },
        lng: getInitialLanguage(),
        fallbackLng: "en",
        supportedLngs: ["vi", "en"],
        ns: ["account", "admin", "auth", "common", "feedback", "layout", "notifications", "surveys", "surveyResults", "validation"],
        defaultNS: "common",
        detection: {
            order: ["localStorage", "htmlTag", "navigator"],
            lookupLocalStorage: LANGUAGE_STORAGE_KEY,
            caches: ["localStorage"],
        },
        interpolation: {
            escapeValue: false,
        },
    });

export default i18n;
