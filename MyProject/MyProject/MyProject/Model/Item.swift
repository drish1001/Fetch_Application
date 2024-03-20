//
//  Item.swift
//  MyProject
//
//  Created by Drishti Singh on 3/20/24.
//

import Foundation

struct Item: Decodable, Identifiable {
    let id: Int
    let listId: Int
    let name: String?
}

