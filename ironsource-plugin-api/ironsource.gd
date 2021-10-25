extends Node
class_name IronSource , "res://ironsource-plugin-api/ironsrc_icon.png"

# Godot IronSource mobile ad plugin library
# Interstitial , Rewarded ads and Banner implementation

# Interstitial signals
signal interstitial_loaded
signal interstitial_opened
signal interstitial_closed

# Rewarded Signals
signal rewarded_availability_changed(availability)
signal rewarded_opened
signal rewarded_closed
signal rewarded #reward the player

# Banner
signal banner_loaded

# Plugin
signal plugin_error(error)


# Properties
export var _app_key : String = "10b144b85"
export var _banner_on_top : bool = false
export var _is_personalized : bool = true

var _is_interstitial_loaded : bool = false setget , is_interstitial_loaded
var _is_rewarded_loaded : bool = false setget , is_rewarded_loaded
var _is_banner_visible : bool = false setget , is_banner_visible
var _can_show_ads : bool = true

var _ironsrc : Object = null

onready var _ads_cap_timer : Timer = get_node("ads_cap")

func _enter_tree() -> void:
	if not _initialize():
		printerr("GodotIronSource Plugin not found")


func init_interstitial() -> void:
	if _ironsrc != null:
		_ironsrc.initInterstitial()


func load_interstitial() -> void:
	if _ironsrc != null:
		_ironsrc.loadInterstitial()


func show_interstitial() -> void:
	if _ironsrc != null and _can_show_ads:
		_ironsrc.showInterstitial()


func init_rewarded() -> void:
	if _ironsrc != null:
		_ironsrc.initRewarded()


func show_rewarded() -> void:
	if _ironsrc != null:
		_ironsrc.showRewarded()


func init_banner() -> void:
	if _ironsrc != null:
		_ironsrc.initBanner(_banner_on_top)


func show_banner() -> void:
	if _ironsrc != null:
		_is_banner_visible = true
		_ironsrc.showBanner()


func hide_banner() -> void:
	if _ironsrc != null:
		_is_banner_visible = false
		_ironsrc.hideBanner()


func is_interstitial_loaded() -> bool:
	if not _is_interstitial_loaded:
		load_interstitial()
	return _is_interstitial_loaded and _can_show_ads


func is_rewarded_loaded() -> bool:
	if not _is_rewarded_loaded:
		init_rewarded()
	return _is_rewarded_loaded


func is_banner_visible() -> bool:
	return _is_banner_visible


func _initialize() -> bool:
	if Engine.has_singleton("GodotIronSource"):
		_ironsrc = Engine.get_singleton("GodotIronSource")
		if not _ironsrc.is_connected("on_plugin_error",self,"_on_plugin_error"):
			_connect_signals()
		_ironsrc.init(_app_key,_is_personalized)
		init_interstitial()
		init_rewarded()
#		init_banner()
		return true
	return false


func _connect_signals() -> void:
	_ironsrc.connect("on_plugin_error",self,"_on_plugin_error")
	# Interstitial
	_ironsrc.connect("on_interstitial_loaded",self,"_on_interstitial_loaded")
	_ironsrc.connect("on_interstitial_opened",self,"_on_interstitial_opened")
	_ironsrc.connect("on_interstitial_closed",self,"_on_interstitial_closed")
	# Rewarded
	_ironsrc.connect("on_rewarded_availability_changed",self,"_on_rewarded_availability_changed")
	_ironsrc.connect("on_rewarded_opened",self,"_on_rewarded_opened")
	_ironsrc.connect("on_rewarded_closed",self,"_on_rewarded_closed")
	_ironsrc.connect("on_rewarded",self,"_on_rewarded")
	# Banner
	_ironsrc.connect("on_banner_loaded",self,"_on_banner_loaded")


func _on_plugin_error(error : String) -> void:
	print("GodotIronSource Plugin : " + error)
#	emit_signal("plugin_error",error)


func _on_interstitial_loaded() -> void:
	_is_interstitial_loaded = true
	emit_signal("interstitial_loaded")


func _on_interstitial_opened() -> void:
	_is_interstitial_loaded = false
	emit_signal("interstitial_opened")


func _on_interstitial_closed() -> void:
	_is_interstitial_loaded = false
	_reset_ads_cap_time()
	emit_signal("interstitial_closed")


func _on_rewarded_availability_changed(availability : bool) -> void:
	_is_rewarded_loaded = availability
	emit_signal("rewarded_availability_changed",availability)


func _on_rewarded_opened() -> void:
	emit_signal("rewarded_opened")


func _on_rewarded_closed() -> void:
	emit_signal("rewarded_closed")


func _on_rewarded() -> void:
	_reset_ads_cap_time()
	emit_signal("rewarded")

func _on_banner_loaded() -> void:
	_is_banner_visible = true
	emit_signal("banner_loaded")


func _reset_ads_cap_time() -> void:
	_can_show_ads = false
	_ads_cap_timer.start()


func _on_ads_cap_timeout() -> void:
	_can_show_ads = true
