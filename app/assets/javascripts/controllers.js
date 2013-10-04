'use strict';

/** Controllers */
angular.module('sseChat.controllers', ['sseChat.services']).
    controller('ChatCtrl', function ($scope, $http, chatModel) {
        $scope.rooms = chatModel.getRooms();
        $scope.msgs = [];
        $scope.inputText = "";
        $scope.user = "Jane Doe #" + Math.floor((Math.random() * 100) + 1);
        $scope.currentRoom = $scope.rooms[0];

        /** change current room, restart EventSource connection */
        $scope.setCurrentRoom = function (room) {
            $scope.currentRoom = room;
            $scope.chatFeed.close();
            $scope.msgs = [];
            $scope.listen();
        };

        $scope.status = [{foo: "bar", bar: "baz"}];
        
        // $scope.myData = [{name: "Moroni", age: 50},
        //              {name: "Tiancum", age: 43},
        //              {name: "Jacob", age: 27},
        //              {name: "Nephi", age: 29},
        //              {name: "Enos", age: 34}];
        // $scope.gridOptions = { data : 'myData' };
        
        /** posting chat text to server */
        $scope.submitMsg = function () {
            $http.post("/chat", { text: $scope.inputText, user: $scope.user,
                time: (new Date()).toUTCString(), room: $scope.currentRoom.value });
            $scope.inputText = "";
        };

        /** handle incoming messages: add to messages array */
        $scope.addMsg = function (msg) { 
            $scope.$apply(function () { $scope.msgs.push(JSON.parse(msg.data)); });
            $scope.status = [{other: "changed"}];
            $scope.$apply();
            // $scope.myData = [{ name: msg.data, age: 20}];
            // $scope.gridOptions = { data: 'myData' };
        };

        /** start listening on messages from selected room */
        $scope.listen = function () {
            $scope.chatFeed = new EventSource("/chatFeed/" + $scope.currentRoom.value);
            $scope.chatFeed.addEventListener("message", $scope.addMsg, false);
        };

        $scope.listen();
    });
