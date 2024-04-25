'use client'
import Cookies from 'js-cookie';
import {useEffect, useState} from "react";

export function Test() {
    const [store, setStore] = useState({store: Cookies.get("store")});

    useEffect(() => {
        console.log("store=" + store)
    })
    return (<h1>welcome</h1>)
}