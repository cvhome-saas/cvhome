import {extractSsrContext} from "@/services/store-context-ssr-utils";
import CallbackClient from "./callback-client";

export default async function CallbackPage() {
	const storeContext = await extractSsrContext();

	return <CallbackClient storeContext={storeContext}/>;
}
