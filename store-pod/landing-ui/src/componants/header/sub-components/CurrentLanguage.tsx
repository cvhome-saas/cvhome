'use client'
import {useEffect, useState} from "react";
import Cookies from "js-cookie";
import {NEXT_LOCALE_KEY} from "@/componants/header/HeaderTop";

export const CurrentLanguage = () => {
    const [local, setLocal] = useState("");
    useEffect(() => {
        setLocal(Cookies.get(NEXT_LOCALE_KEY))
    })
    return <>{local}</>
}
