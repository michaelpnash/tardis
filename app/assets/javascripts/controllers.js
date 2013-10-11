'use strict';

/** Controllers */
angular.module('sseChat.controllers', ['sseChat.services']).
    controller('ChatCtrl', function ($scope, $http, chatModel) {
        $scope.items = [];
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

        $scope.status = [];
                
        /** posting chat text to server */
        $scope.submitMsg = function () {
            $http.post("/chat", { text: $scope.inputText, user: $scope.user,
                time: (new Date()).toUTCString(), room: $scope.currentRoom.value });
            $scope.inputText = "";
        };

        /** handle incoming messages: add to messages array */
        $scope.addMsg = function (msg) { 
            //$scope.$apply(function () { $scope.msgs.push(JSON.parse(msg.data)); });
            //$scope.status = JSON.parse(msg.data);
            //$scope.items.push(JSON.parse(msg.data));
            $scope.$apply(function() {
                $scope.items.push(JSON.parse(msg.data));
                var newStat = JSON.parse(msg.data);
                for (var i in $scope.items) {
                  if ($scope.items[i].id == newStat.id) {
                        $scope.items[i] = newStat;
                    }
                 }
            });
            //$scope.$apply();
        };

        /** start listening on messages from selected room */
        $scope.listen = function () {
            $scope.chatFeed = new EventSource("/chatFeed/" + $scope.currentRoom.value);
            $scope.chatFeed.addEventListener("message", $scope.addMsg, false);
        };

        $scope.listen();
    });
