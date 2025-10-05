package org.codeInge.models;


import lombok.*;

import java.util.ArrayList;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConversationChatContext {


    private String currentNoteTitle ;

    private String currentNoteText ;

    private int currentGroupPagination ;

    private ArrayList<ArrayList<String>> notesList ;

}
