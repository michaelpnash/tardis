'use strict';

/** Controllers */
angular.module('sseChat.controllers', ['sseChat.services']).
    controller('ChatCtrl', function ($scope, $http, chatModel, $log) {
    	$log.log("ChatCtrl controller initialized");
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

        /** handle incoming messages: add to messages array */
        $scope.addMsg = function (msg) { 
            $scope.$apply(function() {
                var newStat = JSON.parse(msg.data);
                var found = 0;
                for (var i in $scope.items) {
                  if ($scope.items[i].id == newStat.id) {
                        $scope.items[i] = newStat;
                        found = 1;
                  }
                }
                if (found == 0) {
                    $scope.items.push(newStat);
                }
            });
        };

        /** start listening on messages from selected room */
        $scope.listen = function () {
            $scope.chatFeed = new EventSource("/chatFeed/" + $scope.currentRoom.value);
            $scope.chatFeed.addEventListener("message", $scope.addMsg, false);
        };

        $scope.listen();
    });
