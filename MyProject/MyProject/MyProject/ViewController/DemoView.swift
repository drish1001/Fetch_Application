//
//  DemoView.swift
//  MyProject
//
//  Created by Drishti Singh on 3/20/24.
//

import SwiftUI

struct DemoView: View {
    @ObservedObject var dataService = DataService()
    @State private var selectedSortOption: SortOption = .byName
    @State private var selectedListId: Int? = nil
    @State private var listIds: [Int] = []

    var body: some View {
        NavigationView {
            VStack {
                // Picker for sorting options
                Picker("Sort by", selection: $selectedSortOption) {
                    Text("Name").tag(SortOption.byName)
                    Text("List ID").tag(SortOption.byListId)
                }
                
                .pickerStyle(SegmentedPickerStyle())
                .padding()
                .onChange(of: selectedSortOption) { newValue in
                dataService.sortItems(option: newValue)
                }

                // Picker for selecting listId, visible only when sorting by List ID
                if selectedSortOption == .byListId {
                    Picker("Select List ID", selection: $selectedListId) {
                        Text("All").tag(nil as Int?)
                        ForEach(listIds, id: \.self) { id in
                            Text("\(id)").tag(id as Int?)
                        }
                    }
                    .pickerStyle(MenuPickerStyle())
                    .padding()
                }

                List {
                    if selectedSortOption == .byName {
                        // Display all items sorted by name
                        let allItemsSortedByName = dataService.groupedItems.values
                            .flatMap { $0 }
                            .filter { !$0.name!.isEmpty }
                            .sorted { $0.name! < $1.name! }

                        ForEach(allItemsSortedByName, id: \.id) { item in
                            Text("\(item.name!) (ID: \(item.id), List ID: \(item.listId))")
                        }
                    } else if let listId = selectedListId, let items = dataService.groupedItems[listId] {
                        // Display items for the selected listId
                        ForEach(items, id: \.id) { item in
                            Text(item.name ?? "")
                        }
                    } else {
                        // Display items for all listIds if no specific listId is selected, sorted by List ID
                        ForEach(Array(dataService.groupedItems.keys.sorted()), id: \.self) { listId in
                            Section(header: Text("List ID \(listId)")) {
                                ForEach(dataService.groupedItems[listId]!, id: \.id) { item in
                                    Text(item.name ?? "")
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Item List")
            .onAppear {
                dataService.fetchData()
            }

            .onReceive(dataService.$groupedItems) { _ in
                self.listIds = Array(dataService.groupedItems.keys).sorted()

                if listIds.isEmpty {
                    selectedListId = nil
                } else if selectedListId == nil, selectedSortOption == .byListId {
                    selectedListId = listIds.first
                }
            }
        }
    }
}


#Preview {
    DemoView()
}

