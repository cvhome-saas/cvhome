'use client'
import {useRouter} from "next/navigation";
import {Language, NEXT_LOCALE_KEY} from "@/componants/header/HeaderTop";
import Cookies from "js-cookie";

export const LanguageListSelector = ({languages}: { languages: Language[] }) => {
    const router = useRouter();

    function switchLanguage(it: Language) {
        document.cookie = `${NEXT_LOCALE_KEY}=${it.lang}; path=/; max-age=31536000; SameSite=Lax`;
        Cookies.set(NEXT_LOCALE_KEY, it.lang)
        router.refresh();
        router.push('/' + it.lang);
    }

    return <ul>
        {
            languages.map(it => {
                return <li key={it.lang}>
                    <button onClick={() => switchLanguage(it)}>{it.name}</button>
                </li>
            })
        }
    </ul>
}

