extends Control

var _plugin_name = "GodotLocalNotificationPlugin"
var _android_plugin
@export var web_socket_url : String = "ws://127.0.0.1:9999"
@export var data_to_send : String = "Ping"
@export var received_label : Label
@export var ip_edit : LineEdit

func _ready():
	if Engine.has_singleton(_plugin_name):
		_android_plugin = Engine.get_singleton(_plugin_name)
		_android_plugin.initForegroundNotification("FChannelId", "FChannelName", "FChannelDescription", "Title", "Text")
		_android_plugin.initClassicNotification("BChannelId", "BChannelName", "BChannelDescription", 3)
		_android_plugin.on_websocket_data.connect(_on_websocket_data)
	else:
		printerr("Couldn't find plugin " + _plugin_name)
		
	ip_edit.text = web_socket_url

func _connect_to_server():
	if _android_plugin:
		_android_plugin.connectToServer(ip_edit.text)

func _stop_server():
	if _android_plugin:
		received_label.text = ""
		_android_plugin.disconnect()
		
func _send_data():
	if _android_plugin:
		_android_plugin.sendData(data_to_send)

func _on_websocket_data(data : String):
	received_label.text = "Received data: %s" % data
	if _android_plugin:
		_android_plugin.notify(8888, "New message", data, 0)
