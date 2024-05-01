'use client'
import {useEffect, useState} from "react";
import Cookies from "js-cookie";
import {Store} from "@/types/store";

export const CurrentLanguage = ({store}: { store: Store }) => {
    const NEXT_LOCALE_KEY = 'NEXT_LOCALE';
    const [local, setLocal] = useState(store.defaultLanguage);
    useEffect(() => {
        const cookie = Cookies.get(NEXT_LOCALE_KEY);
        if (cookie) {
            setLocal(cookie);
        }
    }, [])
    return <>{local}</>
}
