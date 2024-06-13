'use client'
import {Language} from "@/componants/header/HeaderTop";
import {usePathname, useRouter} from '@/navigation';
import {Store} from "@/types/store";

export const LanguageListSelector = ({store, languages}: { store: Store, languages: Language[] }) => {
    const router = useRouter();
    const pathname = usePathname();
    const switchLanguage = (it: Language) => {

        router.push(pathname, {locale: it.lang as any});
    };
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

