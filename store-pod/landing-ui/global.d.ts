/* eslint-disable */
type Messsages = typeof import("./messages/en.json")
type FrMesssages = typeof import("./messages/fr.json")


declare interface IntelMessage extends Messsages, FrMesssages{}