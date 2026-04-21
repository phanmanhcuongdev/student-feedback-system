import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import englishFlag from "flag-icons/flags/4x3/gb.svg";
import vietnameseFlag from "flag-icons/flags/4x3/vn.svg";

type LanguageCode = "vi" | "en";

const languages: Array<{
    code: LanguageCode;
    flagSrc: string;
    labels: Record<LanguageCode, string>;
}> = [
    {
        code: "vi",
        flagSrc: vietnameseFlag,
        labels: {
            vi: "Tiếng Việt",
            en: "Vietnamese",
        },
    },
    {
        code: "en",
        flagSrc: englishFlag,
        labels: {
            vi: "English",
            en: "English",
        },
    },
];

function normalizeLanguage(language: string | undefined): LanguageCode {
    return language?.startsWith("en") ? "en" : "vi";
}

export default function LanguageSwitcher() {
    const { i18n, t } = useTranslation("layout");
    const [open, setOpen] = useState(false);
    const rootRef = useRef<HTMLDivElement | null>(null);
    const currentLanguage = normalizeLanguage(i18n.resolvedLanguage || i18n.language);
    const currentOption = languages.find((language) => language.code === currentLanguage) ?? languages[0];

    useEffect(() => {
        function handlePointerDown(event: MouseEvent) {
            if (!rootRef.current?.contains(event.target as Node)) {
                setOpen(false);
            }
        }

        function handleEscape(event: KeyboardEvent) {
            if (event.key === "Escape") {
                setOpen(false);
            }
        }

        document.addEventListener("mousedown", handlePointerDown);
        document.addEventListener("keydown", handleEscape);

        return () => {
            document.removeEventListener("mousedown", handlePointerDown);
            document.removeEventListener("keydown", handleEscape);
        };
    }, []);

    function handleLanguageChange(language: LanguageCode) {
        setOpen(false);
        if (language !== currentLanguage) {
            void i18n.changeLanguage(language);
        }
    }

    return (
        <div className="relative" ref={rootRef}>
            <button
                type="button"
                onClick={() => setOpen((current) => !current)}
                className="inline-flex h-11 items-center gap-2 rounded-2xl border border-slate-200 bg-white px-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:border-slate-300 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
                aria-label={t("layout.languageSwitcher.label")}
                aria-haspopup="listbox"
                aria-expanded={open}
            >
                <img
                    src={currentOption.flagSrc}
                    alt=""
                    className="h-5 w-5 rounded-full object-cover"
                    aria-hidden="true"
                />
                <span className="hidden sm:inline">{currentOption.labels[currentLanguage]}</span>
                <span className="material-symbols-outlined text-[18px] text-slate-500" aria-hidden="true">
                    expand_more
                </span>
            </button>

            {open ? (
                <div
                    className="absolute right-0 top-[calc(100%+0.5rem)] z-50 w-48 overflow-hidden rounded-2xl border border-slate-200 bg-white p-1 shadow-[0_18px_40px_rgba(15,23,42,0.14)]"
                    role="listbox"
                    aria-label={t("layout.languageSwitcher.label")}
                >
                    {languages.map((language) => {
                        const active = language.code === currentLanguage;

                        return (
                            <button
                                key={language.code}
                                type="button"
                                onClick={() => handleLanguageChange(language.code)}
                                className={[
                                    "flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900",
                                    active
                                        ? "bg-slate-900 text-white"
                                        : "text-slate-700 hover:bg-slate-100 hover:text-slate-950",
                                ].join(" ")}
                                role="option"
                                aria-selected={active}
                            >
                                <img
                                    src={language.flagSrc}
                                    alt=""
                                    className="h-5 w-5 rounded-full object-cover"
                                    aria-hidden="true"
                                />
                                <span>{language.labels[currentLanguage]}</span>
                            </button>
                        );
                    })}
                </div>
            ) : null}
        </div>
    );
}
