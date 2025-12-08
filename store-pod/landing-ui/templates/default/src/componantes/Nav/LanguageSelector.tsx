'use client'
import {Store} from "@/types/store";
import {Globe} from "lucide-react";
import {usePathname, useRouter} from "@/i18n/navigation";
import {useTranslations} from "next-intl";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";
import {Button} from "@/components/ui/button";

export const LanguageSelector = ({store, locale}: { store: Store, locale: string }) => {
    const t = useTranslations('COMPONENTS.HEADER.LANGUAGE');
    const router = useRouter();
    const pathname = usePathname();

    if (!store.supportedLanguages || store.supportedLanguages.length <= 1) {
        return null;
    }

    const currentLocale = store.supportedLanguages.includes(locale) ? locale : 'en';

    return (
        <DropdownMenu>
            <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                    <Globe className="size-6"/>
                    <span className="sr-only">{t('CHANGE_LANGUAGE')}</span>
                </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
                {store.supportedLanguages.map((lang) => (
                    <DropdownMenuItem
                        key={lang}
                        onClick={() => router.push(pathname, {locale: lang})}
                        className="cursor-pointer"
                        disabled={lang === currentLocale}
                    >
                        {t(lang.toUpperCase())}
                    </DropdownMenuItem>
                ))}
            </DropdownMenuContent>
        </DropdownMenu>
    );
}