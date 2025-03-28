import React from "react";
import { Ionicons, MaterialCommunityIcons, MaterialIcons, Feather, Octicons, FontAwesome } from "@expo/vector-icons";

export const Icons = {
    edit: (props: any) => <Ionicons name="pencil" size={24} {...props} />,
    delete: (props: any) => <Ionicons name="trash" size={24} {...props} />,
    bookmark: (props: any) => <Ionicons name="bookmark-outline" size={24} color="black" />,
    bookmarkUndo: (props: any) => <Ionicons name="bookmark" size={24} {...props} />,
    report: (props: any) => <MaterialCommunityIcons name="alarm-light-outline" size={24} color="black" />,
    add: (props: any) => <Octicons name="diff-added" size={24} color="black" />,
    share: (props: any) => <Feather name="share" size={24} color="black" {...props} />,
    settings: (props: any) => <Feather name="settings" size={24} color="black" {...props} />,
    logout: (props: any) => <Ionicons name="exit-outline" size={24} color="black" {...props} />,
    send: (props: any) => <MaterialCommunityIcons name="send" size={24} {...props} />,
    write: (props: any) => <Feather name="edit" size={24} color="white" {...props} />,
    heartFill: (props: any) => <FontAwesome name="heart" size={24} color="black" />,
    heartBlank: (props: any) => <FontAwesome name="heart-o" size={24} color="black" />,
    back: (props: any) => <Ionicons name="chevron-back" size={24} color="black" {...props} />,
    leftOptionEmoji: "🙆‍♂️",
    rightOptionEmoji: "🙅",
};
