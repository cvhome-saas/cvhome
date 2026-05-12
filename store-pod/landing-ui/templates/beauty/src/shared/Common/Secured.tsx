'use client'

import {useUser} from "@store-front/hooks/use-user"
import {StoreContext} from "@/types/store-context"
import {ReactNode, useEffect} from "react"
import {useTranslations} from "next-intl"

export const Secured = ({storeContext, children}: { storeContext: StoreContext, children: ReactNode }) => {
    const {user, loading, login} = useUser(storeContext)
    const t = useTranslations('PAGE.CUSTOMER')

    useEffect(() => {
        if (!loading && !user) {
            login()
        }
    }, [user, loading, login])

    if (loading || !user) {
        return (
            <div className="flex items-center justify-center min-h-[400px]">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                    <p className="text-muted-foreground font-medium italic">{t('LOADING')}</p>
                </div>
            </div>
        )
    }

    return <>{children}</>
}
