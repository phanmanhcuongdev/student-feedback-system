import { useLayoutEffect, useRef, useState } from "react";

import type { TranslationContent } from "../../types/feedback";

const DEFAULT_MAX_COLLAPSED_CHARS = 320;

export type SmartTextDisplayProps = TranslationContent & {
    className?: string;
    maxCollapsedChars?: number;
};

function isBlank(value: string | null | undefined) {
    return !value || value.trim().length === 0;
}

function truncateContent(value: string, maxChars: number) {
    if (value.length <= maxChars) {
        return value;
    }

    return `${value.slice(0, maxChars).trimEnd()}...`;
}

function logOriginalToggle(nextShowOriginal: boolean, sourceLang: string) {
    console.info("smart_text_display.toggle_original", {
        action: nextShowOriginal ? "view_original" : "view_translation",
        sourceLang: sourceLang || "unknown",
        timestamp: new Date().toISOString(),
    });
}

export default function SmartTextDisplay({
    displayContent,
    originalContent,
    contentTranslated,
    isAutoTranslated,
    sourceLang,
    className = "",
    maxCollapsedChars = DEFAULT_MAX_COLLAPSED_CHARS,
}: SmartTextDisplayProps) {
    const [showOriginal, setShowOriginal] = useState(false);
    const [isExpanded, setIsExpanded] = useState(false);
    const contentRef = useRef<HTMLParagraphElement>(null);
    const contentMinHeightRef = useRef(0);
    const translatedContent = (isBlank(contentTranslated) ? displayContent : contentTranslated) ?? "";
    const hasValidTranslation = isAutoTranslated && !isBlank(translatedContent);
    const fallbackContent = (!isBlank(originalContent) ? originalContent : displayContent) ?? "";
    const canToggle = hasValidTranslation && !isBlank(originalContent);
    const isWaitingForTranslation = !isAutoTranslated && !contentTranslated;
    const fullContent = hasValidTranslation
        ? (showOriginal && originalContent ? originalContent : translatedContent)
        : fallbackContent;
    const shouldTruncate = fullContent.length > maxCollapsedChars;
    const content = shouldTruncate && !isExpanded
        ? truncateContent(fullContent, maxCollapsedChars)
        : fullContent;
    const toggleLabel = showOriginal ? "Xem ban dich" : "Xem nguyen van";

    function handleToggleOriginal() {
        setShowOriginal((current) => {
            const next = !current;
            logOriginalToggle(next, sourceLang);
            return next;
        });
    }

    useLayoutEffect(() => {
        const contentElement = contentRef.current;
        if (!contentElement) {
            return;
        }

        const height = contentElement.offsetHeight;
        if (height > contentMinHeightRef.current) {
            contentMinHeightRef.current = height;
        }

        const minHeight = contentMinHeightRef.current;
        if (minHeight) {
            const nextMinHeight = `${minHeight}px`;
            if (contentElement.style.minHeight !== nextMinHeight) {
                contentElement.style.minHeight = nextMinHeight;
            }
        }
    }, [content]);

    return (
        <div className={`space-y-2 ${className}`.trim()}>
            <p
                ref={contentRef}
                key={content}
                className="animate-[smartTextFade_180ms_ease-out] whitespace-pre-line text-sm leading-6 text-slate-700"
            >
                {content}
            </p>

            {isWaitingForTranslation ? (
                <span className="inline-flex items-center gap-1.5 rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-xs font-semibold text-amber-700">
                    <span aria-hidden="true">{"\u{1F504}"}</span>
                    <span>Dang cho dich...</span>
                </span>
            ) : null}

            {canToggle ? (
                <div className="group relative inline-flex">
                    <button
                        type="button"
                        onClick={handleToggleOriginal}
                        className="inline-flex items-center gap-1.5 rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-700 transition hover:border-sky-300 hover:bg-sky-100 focus:outline-none focus:ring-2 focus:ring-sky-200"
                        aria-label={toggleLabel}
                    >
                        <span aria-hidden="true">{"\u{1F310}"}</span>
                        <span>{toggleLabel}</span>
                        {sourceLang ? (
                            <span className="ml-1 rounded-full bg-white/80 px-1.5 py-0.5 text-[10px] font-bold uppercase text-sky-600">
                                {sourceLang}
                            </span>
                        ) : null}
                    </button>
                    <span
                        role="tooltip"
                        className="pointer-events-none absolute left-0 top-full z-10 mt-2 w-max max-w-[260px] translate-y-1 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-600 opacity-0 shadow-lg transition duration-150 group-hover:translate-y-0 group-hover:opacity-100 group-focus-within:translate-y-0 group-focus-within:opacity-100"
                    >
                        Noi dung nay duoc dich tu dong boi AI (Qwen2)
                    </span>
                </div>
            ) : null}

            {shouldTruncate ? (
                <button
                    type="button"
                    onClick={() => setIsExpanded((current) => !current)}
                    className="inline-flex items-center rounded-full border border-slate-200 bg-white px-3 py-1 text-xs font-semibold text-slate-600 transition hover:border-slate-300 hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-slate-200"
                    aria-expanded={isExpanded}
                >
                    {isExpanded ? "Thu gon" : "Xem them"}
                </button>
            ) : null}
        </div>
    );
}
