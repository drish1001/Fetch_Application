//
//  DataService.swift
//  MyProject
//
//  Created by Drishti Singh on 3/20/24.
//

import Foundation

enum SortOption {
    case byListId
    case byName
}

class DataService: ObservableObject {
    @Published var groupedItems: [Int: [Item]] = [:]

    func fetchData() {
        guard let url = URL(string: "https://fetch-hiring.s3.amazonaws.com/hiring.json") else {
            print("Invalid URL")
            return
        }
        
        let task = URLSession.shared.dataTask(with: url) { [weak self] data, _, error in
            guard let data = data, error == nil else {
                print("Data fetch error: \(String(describing: error))")
                return
            }
            
            do {
                let items = try JSONDecoder().decode([Item].self, from: data)
                DispatchQueue.main.async {
                    self?.processItems(items)
                }
            } catch {
                print("Decoding error: \(error)")
            }
        }
        task.resume()
    }
    
    private func processItems(_ items: [Item]) {
        let filteredItems = items.filter { $0.name != nil && !$0.name!.isEmpty }
        let sortedItems = filteredItems.sorted {
            $0.listId == $1.listId ? $0.name! < $1.name! : $0.listId < $1.listId
        }
        groupedItems = Dictionary(grouping: sortedItems, by: { $0.listId })
    }
    
    
    func sortItems(option: SortOption) {
        switch option {
        case .byListId:
            let sorted = groupedItems.mapValues { $0.sorted { $0.listId < $1.listId } }
            groupedItems = sorted
        case .byName:
            let sorted = groupedItems.mapValues { $0.sorted { $0.name! < $1.name! } }
            groupedItems = sorted
        }
    }

}

