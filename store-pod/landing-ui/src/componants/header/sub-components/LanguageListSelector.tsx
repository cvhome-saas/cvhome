'use client'
import {Language} from "@/componants/header/HeaderTop";
import {usePathname, useRouter} from '@/navigation';

export const LanguageListSelector = ({languages}: { languages: Language[] }) => {
    const router = useRouter();
    const pathname = usePathname();
    const switchLanguage = (it: Language) => router.push(pathname, {locale: it.lang});

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

