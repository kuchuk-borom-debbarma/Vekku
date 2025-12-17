
export type ContentRegionTags = {
    regionContent: string
    regionStartIndex: number
    regionEndIndex: number
    tagScores: TagScore[]
}

export type TagScore = {
    name: string
    score: number
}

export type SuggestTagsResponse = {
    regions: ContentRegionTags[]
    overallTags: TagScore[]
}