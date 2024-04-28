'use client'
import {useEffect, useState} from "react";
import Cookies from "js-cookie";
import {Store} from "@/types/store";

export const CurrentLanguage = ({store}: { store: Store }) => {
    const NEXT_LOCALE_KEY = 'NEXT_LOCALE';
    const [local, setLocal] = useState(store.defaultLanguage);
    useEffect(() => {
        setLocal(Cookies.get(NEXT_LOCALE_KEY))
    })
    return <>{local}</>
}
